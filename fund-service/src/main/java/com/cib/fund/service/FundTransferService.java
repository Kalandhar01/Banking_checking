package com.cib.fund.service;

import com.cib.fund.dto.FundTransferRequest;
import com.cib.fund.dto.FundTransferResponse;
import com.cib.fund.dto.TransactionAuditResponse;

import java.util.List;

public interface FundTransferService {
    FundTransferResponse initiateTransfer(FundTransferRequest request);
    FundTransferResponse completeTransaction(Long transactionId, String approvedBy);
    FundTransferResponse failTransaction(Long transactionId, String approvedBy, String reason);
    FundTransferResponse rejectTransaction(Long transactionId, String rejectedBy, String reason);
    FundTransferResponse getTransactionById(Long id);
    List<FundTransferResponse> getTransactionsByCustomerId(Long customerId);
    List<TransactionAuditResponse> getTransactionAudit(Long transactionId);
    List<FundTransferResponse> getPendingLevel1();
    List<FundTransferResponse> getPendingLevel2();
    List<FundTransferResponse> getAllPending();
    FundTransferResponse approveLevel1(Long transactionId, String checkerId);
    FundTransferResponse approveLevel2(Long transactionId, String checkerId);
}
