package com.cib.approval.service;

import com.cib.approval.dto.ApprovalActionRequest;
import com.cib.approval.dto.FundTransactionDto;

import java.util.List;

public interface ApprovalService {
    FundTransactionDto actOnTransaction(Long transactionId, ApprovalActionRequest request);
    List<FundTransactionDto> getPendingApprovals();
    FundTransactionDto getTransactionDetails(Long transactionId);
}
