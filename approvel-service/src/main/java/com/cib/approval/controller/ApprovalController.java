package com.cib.approval.controller;

import com.cib.approval.dto.*;
import com.cib.approval.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ApprovalResponse>> submitForApproval(
            @Valid @RequestBody ApprovalRequestDto request) {
        ApprovalResponse response = approvalService.submitForApproval(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Transaction " + response.getTransactionId() + " submitted for approval. Approval ID: " + response.getId(),
                        response));
    }

    @PutMapping("/{transactionId}/{checkerId}/approve")
    public ResponseEntity<ApiResponse<ApprovalResponse>> approveTransaction(
            @PathVariable Long transactionId,
            @PathVariable String checkerId) {
        ApprovalResponse response = approvalService.approveTransactionByTxId(transactionId, checkerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + transactionId + " approved successfully by checker " + checkerId,
                response));
    }

    @PutMapping("/{transactionId}/{checkerId}/reject")
    public ResponseEntity<ApiResponse<ApprovalResponse>> rejectTransaction(
            @PathVariable Long transactionId,
            @PathVariable String checkerId,
            @RequestParam String reason) {
        ApprovalResponse response = approvalService.rejectTransactionByTxId(transactionId, checkerId, reason);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + transactionId + " rejected by checker " + checkerId + ". Reason: " + reason,
                response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingApprovals() {
        List<ApprovalResponse> response = approvalService.getPendingApprovals();
        String message = response.isEmpty()
                ? "No pending approvals found"
                : "Pending approvals retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/checker/{checkerId}")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getApprovalsByChecker(
            @PathVariable String checkerId) {
        List<ApprovalResponse> response = approvalService.getApprovalsByChecker(checkerId);
        String message = response.isEmpty()
                ? "No approvals found for checker ID " + checkerId
                : "Approvals retrieved for checker ID " + checkerId + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalResponse>> getApprovalById(@PathVariable Long id) {
        ApprovalResponse response = approvalService.getApprovalById(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Approval ID " + id + " for transaction " + response.getTransactionId() + " found", response));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransactionDto>> getTransactionDetails(
            @PathVariable Long transactionId) {
        FundTransactionDto response = approvalService.getTransactionDetails(transactionId);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + transactionId + " details retrieved", response));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getApprovalsByLevel(
            @PathVariable String level) {
        List<ApprovalResponse> response = approvalService.getApprovalsByCheckerLevel(level);
        String message = response.isEmpty()
                ? "No approvals found for level " + level
                : "Approvals retrieved for level " + level + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
