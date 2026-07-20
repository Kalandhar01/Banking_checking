package com.cib.fund.service.impl;

import com.cib.fund.dto.*;
import com.cib.fund.entity.FundTransaction;
import com.cib.fund.entity.TransactionAudit;
import com.cib.fund.enums.TransactionStatus;
import com.cib.fund.exception.InvalidTransactionException;
import com.cib.fund.exception.ResourceNotFoundException;
import com.cib.fund.feign.BeneficiaryClient;
import com.cib.fund.feign.CustomerAccountClient;
import com.cib.fund.feign.CustomerUserClient;
import com.cib.fund.repository.FundTransactionRepository;
import com.cib.fund.repository.TransactionAuditRepository;
import com.cib.fund.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("100000");

    private final FundTransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final BeneficiaryClient beneficiaryClient;
    private final CustomerAccountClient customerAccountClient;
    private final CustomerUserClient customerUserClient;

    @Override
    @Transactional
    public FundTransferResponse initiateTransfer(FundTransferRequest request) {
        BeneficiaryValidationResponse validation = beneficiaryClient.validateBeneficiary(request.getBeneficiaryId());

        if (!validation.isSuccess() || Boolean.FALSE.equals(validation.getData())) {
            throw new InvalidTransactionException(
                    "Beneficiary ID " + request.getBeneficiaryId() + " is not ACTIVE. Transfer cannot be initiated.");
        }

        FundTransaction transaction = FundTransaction.builder()
                .userId(request.getUserId())
                .customerId(request.getCustomerId())
                .beneficiaryId(request.getBeneficiaryId())
                .beneficiaryAccount(request.getBeneficiaryAccount())
                .beneficiaryName(request.getBeneficiaryName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .referenceNumber(generateReferenceNumber())
                .status(TransactionStatus.PENDING)
                .initiatedBy(request.getInitiatedBy())
                .build();

        transaction = transactionRepository.save(transaction);
        createAudit(transaction, null, TransactionStatus.PENDING, request.getInitiatedBy(), "Transaction initiated");

        log.info("Transfer initiated: ref={}, amount={}, beneficiary={}",
                transaction.getReferenceNumber(), transaction.getAmount(), transaction.getBeneficiaryName());

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public FundTransferResponse actLevel1(Long transactionId, CheckerActionRequest req) {
        FundTransaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException(
                    "Transaction is not in PENDING state. Current status: " + transaction.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId(), "LEVEL_1");
        boolean requiresLevel2 = transaction.getAmount() != null
                && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            if (requiresLevel2) {
                transaction.setStatus(TransactionStatus.LEVEL1_APPROVED);
                transaction.setApprovedBy(req.getCheckerId());
                transactionRepository.save(transaction);
                createAudit(transaction, TransactionStatus.PENDING, TransactionStatus.LEVEL1_APPROVED,
                        req.getCheckerId(), "Level 1 approved");
                log.info("Transaction {} Level 1 approved by checker {}", transactionId, req.getCheckerId());
            } else {
                executeDebitAndComplete(transaction, req.getCheckerId());
            }
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by Level 1 checker";
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setApprovedBy(req.getCheckerId());
            transaction.setRejectionReason(reason);
            transactionRepository.save(transaction);
            createAudit(transaction, TransactionStatus.PENDING, TransactionStatus.REJECTED,
                    req.getCheckerId(), reason);
            log.info("Transaction {} rejected by Level 1 checker {}", transactionId, req.getCheckerId());
        } else {
            throw new InvalidTransactionException("Invalid action. Must be ACCEPT or REJECT.");
        }

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public FundTransferResponse actLevel2(Long transactionId, CheckerActionRequest req) {
        FundTransaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.LEVEL1_APPROVED) {
            throw new InvalidTransactionException(
                    "Transaction must be in LEVEL1_APPROVED state. Current state: " + transaction.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId(), "LEVEL_2");

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            executeDebitAndComplete(transaction, req.getCheckerId());
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by Level 2 checker";
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setApprovedBy(req.getCheckerId());
            transaction.setRejectionReason(reason);
            transactionRepository.save(transaction);
            createAudit(transaction, TransactionStatus.LEVEL1_APPROVED, TransactionStatus.REJECTED,
                    req.getCheckerId(), reason);
            log.info("Transaction {} rejected by Level 2 checker {}", transactionId, req.getCheckerId());
        } else {
            throw new InvalidTransactionException("Invalid action. Must be ACCEPT or REJECT.");
        }

        return toResponse(transaction);
    }

    private void executeDebitAndComplete(FundTransaction transaction, String checkerId) {
        Long transactionId = transaction.getId();
        var accountResponse = customerAccountClient.getAccountByUserId(transaction.getUserId());
        if (accountResponse == null || !accountResponse.isSuccess() || accountResponse.getData() == null) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setApprovedBy(checkerId);
            transaction.setRejectionReason("Failed to fetch customer account");
            transactionRepository.save(transaction);
            createAudit(transaction, transaction.getStatus(), TransactionStatus.FAILED,
                    checkerId, "Failed to fetch account");
            log.info("Transaction {} failed: unable to fetch account", transactionId);
            return;
        }

        AccountTransactionRequest debitRequest = AccountTransactionRequest.builder()
                .amount(transaction.getAmount())
                .reference(transaction.getReferenceNumber())
                .build();

        var debitResult = customerAccountClient.debitAccount(accountResponse.getData().getId(), debitRequest);
        if (debitResult == null || !debitResult.isSuccess()) {
            String reason = debitResult != null ? debitResult.getMessage() : "Debit failed";
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setApprovedBy(checkerId);
            transaction.setRejectionReason(reason);
            transactionRepository.save(transaction);
            createAudit(transaction, transaction.getStatus(), TransactionStatus.FAILED,
                    checkerId, "Debit failed: " + reason);
            log.info("Transaction {} failed: debit failed", transactionId);
            return;
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setApprovedBy(checkerId);
        transactionRepository.save(transaction);
        createAudit(transaction, transaction.getStatus(), TransactionStatus.APPROVED,
                checkerId, "Transaction completed and debited");
        log.info("Transaction {} fully approved and debited by checker {}", transactionId, checkerId);
    }

    private CustomerUserDto validateChecker(String checkerId, String expectedLevel) {
        CustomerUserDto checker = customerUserClient.getUser(Long.valueOf(checkerId));
        if (checker == null) {
            throw new ResourceNotFoundException("Checker not found with ID: " + checkerId);
        }
        if (!"CHECKER".equalsIgnoreCase(checker.getRole())) {
            throw new InvalidTransactionException("User " + checkerId + " is not a CHECKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(checker.getStatus())) {
            throw new InvalidTransactionException("Checker account is not ACTIVE");
        }
        if (!expectedLevel.equalsIgnoreCase(checker.getCheckerLevel())) {
            throw new InvalidTransactionException(
                    "User " + checkerId + " is not a " + expectedLevel + " checker.");
        }
        return checker;
    }

    @Override
    public List<FundTransferResponse> getPendingLevel1() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<FundTransferResponse> getPendingLevel2() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.LEVEL1_APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FundTransferResponse getTransactionById(Long id) {
        return toResponse(findTransaction(id));
    }

    @Override
    public List<FundTransferResponse> getTransactionsByCustomerId(Long customerId) {
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TransactionAuditResponse> getTransactionAudit(Long transactionId) {
        findTransaction(transactionId);
        return auditRepository.findByTransactionIdOrderByCreatedAtAsc(transactionId)
                .stream()
                .map(this::toAuditResponse)
                .toList();
    }

    private FundTransaction findTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    private void createAudit(FundTransaction transaction, TransactionStatus fromStatus,
                              TransactionStatus toStatus, String changedBy, String comment) {
        TransactionAudit audit = TransactionAudit.builder()
                .transactionId(transaction.getId())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .comment(comment)
                .build();
        auditRepository.save(audit);
    }

    private String generateReferenceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = new Random().nextInt(99999);
        return "TXN" + datePart + randomPart;
    }

    private FundTransferResponse toResponse(FundTransaction txn) {
        return FundTransferResponse.builder()
                .id(txn.getId())
                .userId(txn.getUserId())
                .customerId(txn.getCustomerId())
                .beneficiaryId(txn.getBeneficiaryId())
                .beneficiaryAccount(txn.getBeneficiaryAccount())
                .beneficiaryName(txn.getBeneficiaryName())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .referenceNumber(txn.getReferenceNumber())
                .status(txn.getStatus())
                .initiatedBy(txn.getInitiatedBy())
                .approvedBy(txn.getApprovedBy())
                .rejectionReason(txn.getRejectionReason())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }

    private TransactionAuditResponse toAuditResponse(TransactionAudit audit) {
        return TransactionAuditResponse.builder()
                .id(audit.getId())
                .transactionId(audit.getTransactionId())
                .fromStatus(audit.getFromStatus())
                .toStatus(audit.getToStatus())
                .changedBy(audit.getChangedBy())
                .comment(audit.getComment())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}
