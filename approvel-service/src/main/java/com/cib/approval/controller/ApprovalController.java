package com.cib.approval.controller;

import com.cib.approval.dto.*;
import com.cib.approval.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FundTransactionDto>>> getPendingApprovals() {
        List<FundTransactionDto> response = approvalService.getPendingApprovals();
        return ResponseEntity.ok(ApiResponse.success(
                "Pending approvals retrieved. Count: " + response.size(), response));
    }

    @GetMapping("/pending/level-1")
    public ResponseEntity<ApiResponse<List<FundTransactionDto>>> getPendingLevel1() {
        List<FundTransactionDto> response = approvalService.getPendingLevel1();
        return ResponseEntity.ok(ApiResponse.success(
                "Level 1 pending approvals retrieved. Count: " + response.size(), response));
    }

    @GetMapping("/pending/level-2")
    public ResponseEntity<ApiResponse<List<FundTransactionDto>>> getPendingLevel2() {
        List<FundTransactionDto> response = approvalService.getPendingLevel2();
        return ResponseEntity.ok(ApiResponse.success(
                "Level 2 pending approvals retrieved. Count: " + response.size(), response));
    }

    @PutMapping("/level-1/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransactionDto>> actLevel1(
            @PathVariable Long transactionId,
            @Valid @RequestBody ApprovalActionRequest request) {
        FundTransactionDto response = approvalService.actLevel1(transactionId, request);
        String msg = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? "Transaction " + transactionId + " accepted by Level 1 checker " + request.getCheckerId()
                : "Transaction " + transactionId + " rejected by Level 1 checker " + request.getCheckerId();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PutMapping("/level-2/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransactionDto>> actLevel2(
            @PathVariable Long transactionId,
            @Valid @RequestBody ApprovalActionRequest request) {
        FundTransactionDto response = approvalService.actLevel2(transactionId, request);
        String msg = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? "Transaction " + transactionId + " accepted by Level 2 checker " + request.getCheckerId()
                : "Transaction " + transactionId + " rejected by Level 2 checker " + request.getCheckerId();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransactionDto>> getTransactionDetails(
            @PathVariable Long transactionId) {
        FundTransactionDto response = approvalService.getTransactionDetails(transactionId);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + transactionId + " details retrieved", response));
    }
}
