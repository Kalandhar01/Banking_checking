package com.cib.fund.service.impl;

import com.cib.fund.dto.AccountTransactionRequest;
import com.cib.fund.dto.BeneficiaryValidationResponse;
import com.cib.fund.dto.FundTransferRequest;
import com.cib.fund.dto.FundTransferResponse;
import com.cib.fund.dto.TransactionAuditResponse;
import com.cib.fund.entity.FundTransaction;
import com.cib.fund.entity.TransactionAudit;
import com.cib.fund.enums.TransactionStatus;
import com.cib.fund.exception.InvalidTransactionException;
import com.cib.fund.exception.ResourceNotFoundException;
import com.cib.fund.feign.BeneficiaryClient;
import com.cib.fund.feign.CustomerAccountClient;
import com.cib.fund.repository.FundTransactionRepository;
import com.cib.fund.repository.TransactionAuditRepository;
import com.cib.fund.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    private final FundTransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final BeneficiaryClient beneficiaryClient;
    private final CustomerAccountClient customerAccountClient;

    @Override
    @Transactional
    public FundTransferResponse initiateTransfer(FundTransferRequest request) {
        BeneficiaryValidationResponse validation = beneficiaryClient.validateBeneficiary(request.getBeneficiaryId());

        if (!validation.isSuccess() || Boolean.FALSE.equals(validation.getData())) {
            throw new InvalidTransactionException(
                    "Beneficiary ID " + request.getBeneficiaryId() + " is not ACTIVE. Transfer cannot be initiated.");
        }

        FundTransaction transaction = FundTransaction.builder()
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
    public FundTransferResponse approveTransaction(Long transactionId, String approvedBy) {
        FundTransaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING
                && transaction.getStatus() != TransactionStatus.MODIFIED) {
            throw new InvalidTransactionException(
                    "Only PENDING or MODIFIED transactions can be approved. Current status: " + transaction.getStatus());
        }

        String reference = transaction.getReferenceNumber();
        AccountTransactionRequest debitRequest = AccountTransactionRequest.builder()
                .amount(transaction.getAmount())
                .reference(reference)
                .build();

        Long userId = Long.valueOf(transaction.getInitiatedBy());

        var accountResponse = customerAccountClient.getAccountByUserId(userId);
        if (accountResponse == null || !accountResponse.isSuccess() || accountResponse.getData() == null) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setApprovedBy(approvedBy);
            transaction.setRejectionReason("Failed to fetch customer account");
            transaction = transactionRepository.save(transaction);
            createAudit(transaction, transaction.getStatus(), TransactionStatus.FAILED, approvedBy,
                    "Approval failed: unable to fetch account");
            return toResponse(transaction);
        }

        Long accountId = accountResponse.getData().getId();
        var debitResponse = customerAccountClient.debitAccount(accountId, debitRequest);

        if (debitResponse == null || !debitResponse.isSuccess()) {
            String reason = (debitResponse != null) ? debitResponse.getMessage() : "Customer service unavailable";
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setApprovedBy(approvedBy);
            transaction.setRejectionReason(reason);
            transaction = transactionRepository.save(transaction);
            createAudit(transaction, transaction.getStatus(), TransactionStatus.FAILED, approvedBy,
                    "Approval failed: " + reason);
            return toResponse(transaction);
        }

        TransactionStatus fromStatus = transaction.getStatus();
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setApprovedBy(approvedBy);
        transaction = transactionRepository.save(transaction);
        createAudit(transaction, fromStatus, TransactionStatus.COMPLETED, approvedBy,
                "Transaction completed. Amount " + transaction.getAmount() + " debited from account " + accountId);

        log.info("Transaction {} completed. Amount {} debited from account {} by {}",
                reference, transaction.getAmount(), accountId, approvedBy);

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public FundTransferResponse rejectTransaction(Long transactionId, String rejectedBy, String reason) {
        FundTransaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException(
                    "Only PENDING transactions can be rejected. Current status: " + transaction.getStatus());
        }

        TransactionStatus fromStatus = transaction.getStatus();
        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setApprovedBy(rejectedBy);
        transaction.setRejectionReason(reason);
        transaction = transactionRepository.save(transaction);

        createAudit(transaction, fromStatus, TransactionStatus.REJECTED, rejectedBy, reason);

        log.info("Transaction {} rejected by {}. Reason: {}", transaction.getReferenceNumber(), rejectedBy, reason);
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public FundTransferResponse modifyAndResubmit(Long transactionId, FundTransferRequest request) {
        FundTransaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.REJECTED) {
            throw new InvalidTransactionException(
                    "Only REJECTED transactions can be modified and resubmitted. Current status: " + transaction.getStatus());
        }

        TransactionStatus fromStatus = transaction.getStatus();
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setBeneficiaryAccount(request.getBeneficiaryAccount());
        transaction.setBeneficiaryName(request.getBeneficiaryName());
        transaction.setBeneficiaryId(request.getBeneficiaryId());
        transaction.setStatus(TransactionStatus.MODIFIED);
        transaction.setRejectionReason(null);
        transaction = transactionRepository.save(transaction);

        createAudit(transaction, fromStatus, TransactionStatus.MODIFIED, request.getInitiatedBy(), "Transaction modified and resubmitted");

        log.info("Transaction {} modified and resubmitted", transaction.getReferenceNumber());
        return toResponse(transaction);
    }

    @Override
    public FundTransferResponse getTransactionById(Long id) {
        FundTransaction transaction = findTransaction(id);
        return toResponse(transaction);
    }

    @Override
    public List<FundTransferResponse> getTransactionsByCustomerId(Long customerId) {
        List<FundTransaction> transactions = transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return transactions.stream().map(this::toResponse).toList();
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
