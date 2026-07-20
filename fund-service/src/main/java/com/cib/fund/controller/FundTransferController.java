package com.cib.fund.controller;

import com.cib.fund.dto.*;
import com.cib.fund.service.FundTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fund")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<FundTransferResponse>> initiateTransfer(
            @Valid @RequestBody FundTransferRequest request) {
        FundTransferResponse response = fundTransferService.initiateTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Transfer initiated. Reference: " + response.getReferenceNumber() + ", ID: " + response.getId(),
                        response));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<FundTransferResponse>> approveTransaction(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        FundTransferResponse response = fundTransferService.approveTransaction(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + response.getReferenceNumber() + " approved by " + approvedBy,
                response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<FundTransferResponse>> rejectTransaction(
            @PathVariable Long id,
            @RequestParam String approvedBy,
            @RequestParam String reason) {
        FundTransferResponse response = fundTransferService.rejectTransaction(id, approvedBy, reason);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + response.getReferenceNumber() + " rejected by " + approvedBy + ". Reason: " + reason,
                response));
    }

    @PutMapping("/{id}/resubmit")
    public ResponseEntity<ApiResponse<FundTransferResponse>> modifyAndResubmit(
            @PathVariable Long id,
            @Valid @RequestBody FundTransferRequest request) {
        FundTransferResponse response = fundTransferService.modifyAndResubmit(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + response.getReferenceNumber() + " modified and resubmitted",
                response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FundTransferResponse>> getTransactionById(@PathVariable Long id) {
        FundTransferResponse response = fundTransferService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction " + response.getReferenceNumber() + " found. Status: " + response.getStatus(),
                response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<FundTransferResponse>>> getTransactionsByCustomerId(
            @PathVariable Long customerId) {
        List<FundTransferResponse> response = fundTransferService.getTransactionsByCustomerId(customerId);
        String message = response.isEmpty()
                ? "No transactions found for customer ID " + customerId
                : "Transactions retrieved for customer ID " + customerId + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<ApiResponse<List<TransactionAuditResponse>>> getTransactionAudit(
            @PathVariable Long id) {
        List<TransactionAuditResponse> response = fundTransferService.getTransactionAudit(id);
        String message = response.isEmpty()
                ? "No audit records found for transaction ID " + id
                : "Audit trail retrieved for transaction ID " + id + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
