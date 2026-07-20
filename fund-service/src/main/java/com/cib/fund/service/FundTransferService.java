package com.cib.fund.service;

import com.cib.fund.dto.CheckerActionRequest;
import com.cib.fund.dto.FundTransferRequest;
import com.cib.fund.dto.FundTransferResponse;
import com.cib.fund.dto.TransactionAuditResponse;

import java.util.List;

public interface FundTransferService {
    FundTransferResponse initiateTransfer(FundTransferRequest request);
    FundTransferResponse actLevel1(Long transactionId, CheckerActionRequest request);
    FundTransferResponse actLevel2(Long transactionId, CheckerActionRequest request);
    FundTransferResponse getTransactionById(Long id);
    List<FundTransferResponse> getTransactionsByCustomerId(Long customerId);
    List<FundTransferResponse> getPendingLevel1();
    List<FundTransferResponse> getPendingLevel2();
    List<TransactionAuditResponse> getTransactionAudit(Long transactionId);
}
