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

    @GetMapping("/pending/level-1")
    public ResponseEntity<ApiResponse<List<FundTransferResponse>>> getPendingLevel1() {
        List<FundTransferResponse> response = fundTransferService.getPendingLevel1();
        String msg = response.isEmpty()
                ? "No Level 1 pending transactions found"
                : "Level 1 pending transactions retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @GetMapping("/pending/level-2")
    public ResponseEntity<ApiResponse<List<FundTransferResponse>>> getPendingLevel2() {
        List<FundTransferResponse> response = fundTransferService.getPendingLevel2();
        String msg = response.isEmpty()
                ? "No Level 2 pending transactions found"
                : "Level 2 pending transactions retrieved. Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PutMapping("/level-1/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransferResponse>> actLevel1(
            @PathVariable Long transactionId,
            @Valid @RequestBody CheckerActionRequest request) {
        FundTransferResponse response = fundTransferService.actLevel1(transactionId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Level 1 action completed for transaction " + transactionId, response));
    }

    @PutMapping("/level-2/{transactionId}")
    public ResponseEntity<ApiResponse<FundTransferResponse>> actLevel2(
            @PathVariable Long transactionId,
            @Valid @RequestBody CheckerActionRequest request) {
        FundTransferResponse response = fundTransferService.actLevel2(transactionId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Level 2 action completed for transaction " + transactionId, response));
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
        String msg = response.isEmpty()
                ? "No transactions found for customer ID " + customerId
                : "Transactions retrieved for customer ID " + customerId + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<ApiResponse<List<TransactionAuditResponse>>> getTransactionAudit(
            @PathVariable Long id) {
        List<TransactionAuditResponse> response = fundTransferService.getTransactionAudit(id);
        String msg = response.isEmpty()
                ? "No audit records found for transaction ID " + id
                : "Audit trail retrieved for transaction ID " + id + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}
