package com.cib.approval.service.impl;

import com.cib.approval.dto.*;
import com.cib.approval.exception.InvalidApprovalException;
import com.cib.approval.exception.ResourceNotFoundException;
import com.cib.approval.feign.BeneficiaryClient;
import com.cib.approval.feign.CustomerServiceClient;
import com.cib.approval.feign.FundServiceClient;
import com.cib.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final FundServiceClient fundServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final BeneficiaryClient beneficiaryClient;

    @Override
    public FundTransactionDto actOnTransaction(Long transactionId, ApprovalActionRequest req) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new InvalidApprovalException(
                    "Transaction is not in PENDING state. Current state: " + transaction.getStatus());
        }

        CustomerUserDto checker = validateChecker(req.getCheckerId());

        if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
            String validationError = validateTransactionEntities(transaction);
            if (validationError != null) {
                fundServiceClient.rejectTransaction(transactionId, req.getCheckerId(), validationError);
                log.info("Transaction {} rejected: {}", transactionId, validationError);
                return fundServiceClient.getTransaction(transactionId);
            }

            FundTransactionDto approved = fundServiceClient.approveTransaction(transactionId, req.getCheckerId());
            if (approved == null) {
                throw new RuntimeException("Approval failed for transaction " + transactionId);
            }
            executeDebitAndComplete(transactionId, transaction.getUserId(),
                    transaction.getAmount(), transaction.getReferenceNumber(), req.getCheckerId());
            log.info("Transaction {} approved by checker {}", transactionId, req.getCheckerId());
            return fundServiceClient.getTransaction(transactionId);
        } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
            String reason = req.getRemarks() != null ? req.getRemarks() : "Rejected by checker";
            fundServiceClient.rejectTransaction(transactionId, req.getCheckerId(), reason);
            log.info("Transaction {} rejected by checker {}. Reason: {}", transactionId, req.getCheckerId(), reason);
            return fundServiceClient.getTransaction(transactionId);
        } else {
            throw new InvalidApprovalException("Invalid action. Must be ACCEPT or REJECT.");
        }
    }

    private String validateTransactionEntities(FundTransactionDto transaction) {
        CustomerUserDto maker = customerServiceClient.getUser(transaction.getUserId());
        if (maker == null || !"ACTIVE".equalsIgnoreCase(maker.getStatus())) {
            return "Maker (user ID " + transaction.getUserId() + ") is not ACTIVE";
        }

        CorporateCustomerResponse customer = customerServiceClient.getCustomer(transaction.getCustomerId());
        if (customer == null || !"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            return "Customer (ID " + transaction.getCustomerId() + ") is not ACTIVE";
        }

        if (!beneficiaryClient.isBeneficiaryActive(transaction.getBeneficiaryId())) {
            return "Beneficiary (ID " + transaction.getBeneficiaryId() + ") is not ACTIVE";
        }

        return null;
    }

    private void executeDebitAndComplete(Long transactionId, Long userId,
                                          java.math.BigDecimal amount, String referenceNumber, String checkerId) {
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
    public FundTransactionDto getTransactionDetails(Long transactionId) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + transactionId);
        }
        return transaction;
    }
}
