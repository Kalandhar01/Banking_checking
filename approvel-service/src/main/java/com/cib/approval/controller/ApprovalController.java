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

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingApprovals() {
        List<ApprovalResponse> response = approvalService.getPendingApprovals();
        String message = response.isEmpty()
                ? "No pending approvals found"
                : "Pending approvals retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/pending/level-1")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingLevel1() {
        List<ApprovalResponse> response = approvalService.getPendingLevel1();
        String message = response.isEmpty()
                ? "No Level 1 pending approvals found"
                : "Level 1 pending approvals retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/pending/level-2")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingLevel2() {
        List<ApprovalResponse> response = approvalService.getPendingLevel2();
        String message = response.isEmpty()
                ? "No Level 2 pending approvals found"
                : "Level 2 pending approvals retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PutMapping("/level-1/{transactionId}")
    public ResponseEntity<ApiResponse<ApprovalResponse>> actLevel1(
            @PathVariable Long transactionId,
            @Valid @RequestBody ApprovalActionRequest request) {
        ApprovalResponse response = approvalService.actLevel1(transactionId, request);
        String msg = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? "Transaction " + transactionId + " accepted by Level 1 checker " + request.getCheckerId()
                : "Transaction " + transactionId + " rejected by Level 1 checker " + request.getCheckerId();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PutMapping("/level-2/{transactionId}")
    public ResponseEntity<ApiResponse<ApprovalResponse>> actLevel2(
            @PathVariable Long transactionId,
            @Valid @RequestBody ApprovalActionRequest request) {
        ApprovalResponse response = approvalService.actLevel2(transactionId, request);
        String msg = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? "Transaction " + transactionId + " accepted by Level 2 checker " + request.getCheckerId()
                : "Transaction " + transactionId + " rejected by Level 2 checker " + request.getCheckerId();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
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
