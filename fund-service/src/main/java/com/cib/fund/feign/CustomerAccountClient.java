package com.cib.fund.feign;

import com.cib.fund.dto.AccountResponse;
import com.cib.fund.dto.AccountTransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerAccountClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://customer-service/account";

    public AccountResponse getAccountByUserId(Long userId) {
        try {
            String url = BASE_URL + "/user/" + userId;
            ResponseEntity<AccountResponse> response = restTemplate.getForEntity(url, AccountResponse.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error fetching account for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public AccountResponse debitAccount(Long accountId, AccountTransactionRequest request) {
        try {
            String url = BASE_URL + "/" + accountId + "/debit";
            HttpEntity<AccountTransactionRequest> entity = new HttpEntity<>(request);
            ResponseEntity<AccountResponse> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, AccountResponse.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error debiting account {}: {}", accountId, e.getMessage());
            return null;
        }
    }

    public AccountResponse creditAccount(Long accountId, AccountTransactionRequest request) {
        try {
            String url = BASE_URL + "/" + accountId + "/credit";
            HttpEntity<AccountTransactionRequest> entity = new HttpEntity<>(request);
            ResponseEntity<AccountResponse> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, AccountResponse.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error crediting account {}: {}", accountId, e.getMessage());
            return null;
        }
    }
}
