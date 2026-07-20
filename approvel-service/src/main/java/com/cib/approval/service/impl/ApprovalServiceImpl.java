package com.cib.approval.service.impl;

import com.cib.approval.dto.*;
import com.cib.approval.exception.InvalidApprovalException;
import com.cib.approval.exception.ResourceNotFoundException;
import com.cib.approval.feign.CustomerServiceClient;
import com.cib.approval.feign.FundServiceClient;
import com.cib.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("100000");

    private final FundServiceClient fundServiceClient;
    private final CustomerServiceClient customerServiceClient;

    @Override
    public FundTransactionDto actLevel1(Long transactionId, ApprovalActionRequest req) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new InvalidApprovalException(
                    "Transaction is not in PENDING state. Current state: " + transaction.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId());
        if (!"LEVEL_1".equalsIgnoreCase(checker.getCheckerLevel())) {
            throw new InvalidApprovalException("Only LEVEL_1 checkers can use this endpoint.");
        }

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            boolean requiresLevel2 = transaction.getAmount() != null
                    && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;

            FundTransactionDto approved = fundServiceClient.approveLevel1(transactionId, req.getCheckerId());
            if (approved == null) {
                throw new RuntimeException("Level 1 approval failed for transaction " + transactionId);
            }

            if (!requiresLevel2) {
                executeDebitAndComplete(transactionId, transaction.getUserId(),
                        transaction.getAmount(), transaction.getReferenceNumber(), req.getCheckerId());
            }
            log.info("Transaction {} Level 1 approved by checker {}", transactionId, req.getCheckerId());
            return fundServiceClient.getTransaction(transactionId);
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by Level 1 checker";
            fundServiceClient.rejectTransaction(transactionId, req.getCheckerId(), reason);
            log.info("Transaction {} rejected by Level 1 checker {}. Reason: {}", transactionId, req.getCheckerId(), reason);
            return fundServiceClient.getTransaction(transactionId);
        } else {
            throw new InvalidApprovalException("Invalid action. Must be ACCEPT or REJECT.");
        }
    }

    @Override
    public FundTransactionDto actLevel2(Long transactionId, ApprovalActionRequest req) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        if (!"LEVEL1_APPROVED".equals(transaction.getStatus())) {
            throw new InvalidApprovalException(
                    "Transaction must be in LEVEL1_APPROVED state. Current state: " + transaction.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId());
        if (!"LEVEL_2".equalsIgnoreCase(checker.getCheckerLevel())) {
            throw new InvalidApprovalException("Only LEVEL_2 checkers can use this endpoint.");
        }

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            FundTransactionDto approved = fundServiceClient.approveLevel2(transactionId, req.getCheckerId());
            if (approved == null) {
                throw new RuntimeException("Level 2 approval failed for transaction " + transactionId);
            }
            executeDebitAndComplete(transactionId, transaction.getUserId(),
                    transaction.getAmount(), transaction.getReferenceNumber(), req.getCheckerId());
            log.info("Transaction {} Level 2 approved by checker {}", transactionId, req.getCheckerId());
            return fundServiceClient.getTransaction(transactionId);
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Sent back by Level 2 checker";
            fundServiceClient.sendBackToLevel1(transactionId, req.getCheckerId(), reason);
            log.info("Transaction {} sent back to Level 1 by checker {}. Reason: {}", transactionId, req.getCheckerId(), reason);
            return fundServiceClient.getTransaction(transactionId);
        } else {
            throw new InvalidApprovalException("Invalid action. Must be ACCEPT or REJECT.");
        }
    }

    private void executeDebitAndComplete(Long transactionId, Long userId,
                                          BigDecimal amount, String referenceNumber, String checkerId) {
        AccountResponse account = customerServiceClient.getAccountByUserId(userId);
        if (account == null) {
            fundServiceClient.failTransaction(transactionId, checkerId, "Failed to fetch customer account");
            log.info("Transaction {} failed: unable to fetch account", transactionId);
            return;
        }

        AccountTransactionRequest debitRequest = AccountTransactionRequest.builder()
                .amount(amount)
                .reference(referenceNumber)
                .build();

        AccountResponse debitResult = customerServiceClient.debitAccount(account.getId(), debitRequest);
        if (debitResult == null) {
            fundServiceClient.failTransaction(transactionId, checkerId, "Debit failed");
            log.info("Transaction {} failed: debit failed", transactionId);
            return;
        }

        fundServiceClient.completeTransaction(transactionId, checkerId);
        log.info("Transaction {} fully completed by checker {}", transactionId, checkerId);
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
    public List<FundTransactionDto> getPendingApprovals() {
        return fundServiceClient.getPendingTransactions();
    }

    @Override
    public List<FundTransactionDto> getPendingLevel1() {
        return fundServiceClient.getPendingLevel1Transactions();
    }

    @Override
    public List<FundTransactionDto> getPendingLevel2() {
        return fundServiceClient.getPendingLevel2Transactions();
    }

    @Override
    public FundTransactionDto getTransactionDetails(Long transactionId) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + transactionId);
        }
        return transaction;
    }
}
