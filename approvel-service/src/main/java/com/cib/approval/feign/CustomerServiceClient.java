package com.cib.approval.feign;

import com.cib.approval.dto.AccountResponse;
import com.cib.approval.dto.AccountTransactionRequest;
import com.cib.approval.dto.ApiResponse;
import com.cib.approval.dto.CustomerUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final RestTemplate restTemplate;
    private static final String USER_URL = "http://customer-service/customer/users";
    private static final String ACCOUNT_URL = "http://customer-service/account";

    public CustomerUserDto getUser(Long userId) {
        try {
            String url = USER_URL + "/" + userId;
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<CustomerUserDto>>() {}
            );
            ApiResponse<CustomerUserDto> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw new RuntimeException("Customer service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public AccountResponse getAccountByUserId(Long userId) {
        try {
            String url = ACCOUNT_URL + "/user/" + userId;
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {}
            );
            ApiResponse<AccountResponse> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw new RuntimeException("Customer service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error fetching account for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public AccountResponse debitAccount(Long accountId, AccountTransactionRequest request) {
        try {
            String url = ACCOUNT_URL + "/" + accountId + "/debit";
            HttpEntity<AccountTransactionRequest> entity = new HttpEntity<>(request);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {}
            );
            ApiResponse<AccountResponse> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw new RuntimeException("Customer service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error debiting account {}: {}", accountId, e.getMessage());
            return null;
        }
    }

    public AccountResponse creditAccount(Long accountId, AccountTransactionRequest request) {
        try {
            String url = ACCOUNT_URL + "/" + accountId + "/credit";
            HttpEntity<AccountTransactionRequest> entity = new HttpEntity<>(request);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {}
            );
            ApiResponse<AccountResponse> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw new RuntimeException("Customer service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error crediting account {}: {}", accountId, e.getMessage());
            return null;
        }
    }
}
