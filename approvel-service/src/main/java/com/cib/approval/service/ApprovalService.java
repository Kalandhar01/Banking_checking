package com.cib.approval.service;

import com.cib.approval.dto.ApprovalRequestDto;
import com.cib.approval.dto.ApprovalResponse;
import com.cib.approval.dto.FundTransactionDto;

import java.util.List;

public interface ApprovalService {
    ApprovalResponse submitForApproval(ApprovalRequestDto request);
    ApprovalResponse approveTransaction(Long approvalId, String checkerId);
    ApprovalResponse rejectTransaction(Long approvalId, String checkerId, String reason);
    List<ApprovalResponse> getPendingApprovals();
    List<ApprovalResponse> getApprovalsByChecker(String checkerId);
    ApprovalResponse getApprovalById(Long id);
    FundTransactionDto getTransactionDetails(Long transactionId);
    List<ApprovalResponse> getApprovalsByCheckerLevel(String level);
}
