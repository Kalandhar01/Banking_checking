package com.cib.customer.service;

import com.cib.customer.dto.AccountResponse;
import com.cib.customer.dto.AccountTransactionRequest;
import com.cib.customer.dto.ApiResponse;

public interface AccountService {
    ApiResponse<AccountResponse> getAccountByUserId(Long userId);
    ApiResponse<AccountResponse> debitAccount(Long accountId, AccountTransactionRequest request);
    ApiResponse<AccountResponse> creditAccount(Long accountId, AccountTransactionRequest request);
}
