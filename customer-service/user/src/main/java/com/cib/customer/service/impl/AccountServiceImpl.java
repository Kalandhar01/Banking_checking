package com.cib.customer.service.impl;

import com.cib.customer.dto.AccountResponse;
import com.cib.customer.dto.AccountTransactionRequest;
import com.cib.customer.dto.ApiResponse;
import com.cib.customer.entity.CorporateAccount;
import com.cib.customer.entity.CorporateUser;
import com.cib.customer.enums.Status;
import com.cib.customer.exception.InactiveAccountException;
import com.cib.customer.exception.InsufficientBalanceException;
import com.cib.customer.exception.ResourceNotFoundException;
import com.cib.customer.repository.CorporateAccountRepository;
import com.cib.customer.repository.CorporateUserRepository;
import com.cib.customer.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CorporateAccountRepository accountRepository;
    private final CorporateUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AccountResponse> getAccountByUserId(Long userId) {
        CorporateUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        CorporateAccount account = user.getCorporateAccount();
        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Account found for user ID " + userId)
                .data(toResponse(account))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<AccountResponse> debitAccount(Long accountId, AccountTransactionRequest request) {
        CorporateAccount account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getStatus() != Status.ACTIVE) {
            throw new InactiveAccountException("Account is not ACTIVE. Current status: " + account.getStatus());
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + account.getBalance() +
                    ", Requested: " + request.getAmount());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        account = accountRepository.save(account);

        log.info("Account {} debited by {}. Reference: {}. New balance: {}",
                accountId, request.getAmount(), request.getReference(), account.getBalance());

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Debit successful. New balance: " + account.getBalance())
                .data(toResponse(account))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<AccountResponse> creditAccount(Long accountId, AccountTransactionRequest request) {
        CorporateAccount account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getStatus() != Status.ACTIVE) {
            throw new InactiveAccountException("Account is not ACTIVE. Current status: " + account.getStatus());
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        account = accountRepository.save(account);

        log.info("Account {} credited by {}. Reference: {}. New balance: {}",
                accountId, request.getAmount(), request.getReference(), account.getBalance());

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Credit successful. New balance: " + account.getBalance())
                .data(toResponse(account))
                .build();
    }

    private AccountResponse toResponse(CorporateAccount account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .build();
    }
}
