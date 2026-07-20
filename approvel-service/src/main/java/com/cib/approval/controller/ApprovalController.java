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

    @PutMapping("/{transactionId}/act")
    public ResponseEntity<ApiResponse<FundTransactionDto>> actOnTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody ApprovalActionRequest request) {
        FundTransactionDto response = approvalService.actOnTransaction(transactionId, request);
        String msg = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? "Transaction " + transactionId + " accepted by checker " + request.getCheckerId()
                : "Transaction " + transactionId + " rejected by checker " + request.getCheckerId();
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
