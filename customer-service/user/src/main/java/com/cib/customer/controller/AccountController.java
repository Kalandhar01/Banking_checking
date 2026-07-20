package com.cib.customer.controller;

import com.cib.customer.dto.AccountResponse;
import com.cib.customer.dto.AccountTransactionRequest;
import com.cib.customer.dto.ApiResponse;
import com.cib.customer.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountByUserId(userId));
    }

    @PutMapping("/{accountId}/debit")
    public ResponseEntity<ApiResponse<AccountResponse>> debitAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountTransactionRequest request) {
        return ResponseEntity.ok(accountService.debitAccount(accountId, request));
    }

    @PutMapping("/{accountId}/credit")
    public ResponseEntity<ApiResponse<AccountResponse>> creditAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountTransactionRequest request) {
        return ResponseEntity.ok(accountService.creditAccount(accountId, request));
    }
}
