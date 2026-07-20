package com.cib.approval.service;

import com.cib.approval.dto.ApprovalActionRequest;
import com.cib.approval.dto.ApprovalRequestDto;
import com.cib.approval.dto.ApprovalResponse;
import com.cib.approval.dto.FundTransactionDto;

import java.util.List;

public interface ApprovalService {
    ApprovalResponse submitForApproval(ApprovalRequestDto request);
    ApprovalResponse actLevel1(Long transactionId, ApprovalActionRequest request);
    ApprovalResponse actLevel2(Long transactionId, ApprovalActionRequest request);
    List<ApprovalResponse> getPendingApprovals();
    List<ApprovalResponse> getPendingLevel1();
    List<ApprovalResponse> getPendingLevel2();
    List<ApprovalResponse> getApprovalsByChecker(String checkerId);
    ApprovalResponse getApprovalById(Long id);
    FundTransactionDto getTransactionDetails(Long transactionId);
    List<ApprovalResponse> getApprovalsByCheckerLevel(String level);
}
