package com.cib.approval.service.impl;

import com.cib.approval.dto.*;
import com.cib.approval.entity.ApprovalRequest;
import com.cib.approval.enums.ApprovalStatus;
import com.cib.approval.exception.InvalidApprovalException;
import com.cib.approval.exception.ResourceNotFoundException;
import com.cib.approval.feign.CustomerServiceClient;
import com.cib.approval.feign.FundServiceClient;
import com.cib.approval.mapper.ApprovalMapper;
import com.cib.approval.repository.ApprovalRequestRepository;
import com.cib.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("100000");

    private final ApprovalRequestRepository approvalRepository;
    private final FundServiceClient fundServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional
    public ApprovalResponse submitForApproval(ApprovalRequestDto request) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(request.getTransactionId());
        if (transaction == null) {
            throw new ResourceNotFoundException(
                    "Transaction not found with ID: " + request.getTransactionId());
        }

        CustomerUserDto maker = customerServiceClient.getUser(Long.valueOf(request.getMakerId()));
        if (maker == null) {
            throw new ResourceNotFoundException("Maker not found with ID: " + request.getMakerId());
        }
        if (!"MAKER".equalsIgnoreCase(maker.getRole())) {
            throw new InvalidApprovalException(
                    "User " + request.getMakerId() + " is not a MAKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(maker.getStatus())) {
            throw new InvalidApprovalException("Maker account is not ACTIVE");
        }

        if (approvalRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new InvalidApprovalException(
                    "Transaction " + request.getTransactionId() + " is already submitted for approval");
        }

        boolean requiresLevel2 = transaction.getAmount() != null
                && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;

        ApprovalRequest approval = ApprovalRequest.builder()
                .transactionId(request.getTransactionId())
                .makerId(request.getMakerId())
                .makerName(maker.getEmployeeName())
                .level2CheckerId(requiresLevel2 ? "REQUIRED" : null)
                .status(ApprovalStatus.PENDING)
                .build();

        approval = approvalRepository.save(approval);
        log.info("Approval auto-created for transaction {} by maker {}. Level2: {}",
                request.getTransactionId(), request.getMakerId(), requiresLevel2);

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse actLevel1(Long transactionId, ApprovalActionRequest req) {
        ApprovalRequest approval = approvalRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No approval request found for transaction: " + transactionId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new InvalidApprovalException(
                    "Transaction is not in PENDING state. Current state: " + approval.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId());
        if (!"LEVEL_1".equalsIgnoreCase(checker.getCheckerLevel())) {
            throw new InvalidApprovalException("Only LEVEL_1 checkers can use this endpoint.");
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        boolean requiresLevel2 = transaction.getAmount() != null
                && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            if (requiresLevel2) {
                approval.setStatus(ApprovalStatus.LEVEL1_APPROVED);
                approval = approvalRepository.save(approval);
                log.info("Transaction {} Level 1 approved by checker {}", transactionId, req.getCheckerId());
            } else {
                executeFinalApproval(approval, transaction, req.getCheckerId());
            }
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by Level 1 checker";
            fundServiceClient.rejectTransaction(transactionId, req.getCheckerId(), reason);
            approval.setStatus(ApprovalStatus.REJECTED);
            approval.setComments(reason);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} rejected by Level 1 checker {}. Reason: {}", transactionId, req.getCheckerId(), reason);
        } else {
            throw new InvalidApprovalException("Invalid action. Must be ACCEPT or REJECT.");
        }

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse actLevel2(Long transactionId, ApprovalActionRequest req) {
        ApprovalRequest approval = approvalRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No approval request found for transaction: " + transactionId));

        if (approval.getStatus() != ApprovalStatus.LEVEL1_APPROVED) {
            throw new InvalidApprovalException(
                    "Transaction must be in LEVEL1_APPROVED state. Current state: " + approval.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId());
        if (!"LEVEL_2".equalsIgnoreCase(checker.getCheckerLevel())) {
            throw new InvalidApprovalException("Only LEVEL_2 checkers can use this endpoint.");
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            executeFinalApproval(approval, transaction, req.getCheckerId());
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by Level 2 checker";
            fundServiceClient.rejectTransaction(transactionId, req.getCheckerId(), reason);
            approval.setStatus(ApprovalStatus.REJECTED);
            approval.setComments(reason);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} rejected by Level 2 checker {}. Reason: {}", transactionId, req.getCheckerId(), reason);
        } else {
            throw new InvalidApprovalException("Invalid action. Must be ACCEPT or REJECT.");
        }

        return approvalMapper.toResponse(approval);
    }

    private void executeFinalApproval(ApprovalRequest approval, FundTransactionDto transaction, String checkerId) {
        Long transactionId = transaction.getId();
        AccountResponse account = customerServiceClient.getAccountByUserId(transaction.getUserId());
        if (account == null) {
            fundServiceClient.failTransaction(transactionId, checkerId, "Failed to fetch customer account");
            approval.setStatus(ApprovalStatus.FAILED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} failed: unable to fetch account", transactionId);
            return;
        }

        AccountTransactionRequest debitRequest = AccountTransactionRequest.builder()
                .amount(transaction.getAmount())
                .reference(transaction.getReferenceNumber())
                .build();

        AccountResponse debitResult = customerServiceClient.debitAccount(account.getId(), debitRequest);
        if (debitResult == null) {
            fundServiceClient.failTransaction(transactionId, checkerId, "Debit failed");
            approval.setStatus(ApprovalStatus.FAILED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} failed: debit failed", transactionId);
            return;
        }

        fundServiceClient.completeTransaction(transactionId, checkerId);
        approval.setStatus(ApprovalStatus.APPROVED);
        approval = approvalRepository.save(approval);
        log.info("Transaction {} fully approved by checker {}", transactionId, checkerId);
    }

    private CustomerUserDto validateChecker(String checkerId) {
        CustomerUserDto checker = customerServiceClient.getUser(Long.valueOf(checkerId));
        if (checker == null) {
            throw new ResourceNotFoundException("Checker not found with ID: " + checkerId);
        }
        if (!"CHECKER".equalsIgnoreCase(checker.getRole())) {
            throw new InvalidApprovalException("User " + checkerId + " is not a CHECKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(checker.getStatus())) {
            throw new InvalidApprovalException("Checker account is not ACTIVE");
        }
        return checker;
    }

    @Override
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(ApprovalStatus.PENDING, ApprovalStatus.LEVEL1_APPROVED))
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalResponse> getPendingLevel1() {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalResponse> getPendingLevel2() {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.LEVEL1_APPROVED)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalResponse> getApprovalsByChecker(String checkerId) {
        return approvalRepository.findByCheckerIdOrLevel2CheckerIdOrderByCreatedAtDesc(checkerId, checkerId)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public ApprovalResponse getApprovalById(Long id) {
        ApprovalRequest approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with ID: " + id));
        return approvalMapper.toResponse(approval);
    }

    @Override
    public FundTransactionDto getTransactionDetails(Long transactionId) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + transactionId);
        }
        return transaction;
    }

    @Override
    public List<ApprovalResponse> getApprovalsByCheckerLevel(String level) {
        if ("LEVEL_1".equals(level)) {
            return approvalRepository.findByCheckerIdIsNotNullOrderByCreatedAtDesc()
                    .stream()
                    .map(approvalMapper::toResponse)
                    .toList();
        } else {
            return approvalRepository.findByLevel2CheckerIdIsNotNullOrderByCreatedAtDesc()
                    .stream()
                    .map(approvalMapper::toResponse)
                    .toList();
        }
    }
}
