package com.cib.approval.service;

import com.cib.approval.dto.ApprovalActionRequest;
import com.cib.approval.dto.FundTransactionDto;

import java.util.List;

public interface ApprovalService {
    FundTransactionDto actLevel1(Long transactionId, ApprovalActionRequest request);
    FundTransactionDto actLevel2(Long transactionId, ApprovalActionRequest request);
    List<FundTransactionDto> getPendingApprovals();
    List<FundTransactionDto> getPendingLevel1();
    List<FundTransactionDto> getPendingLevel2();
    FundTransactionDto getTransactionDetails(Long transactionId);
}
