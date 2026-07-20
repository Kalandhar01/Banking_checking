package com.cib.approval.feign;

import com.cib.approval.dto.ApiResponse;
import com.cib.approval.dto.FundTransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundServiceClient {

    private final RestTemplate restTemplate;
    private static final String FUND_SERVICE_URL = "http://fund-service/fund";

    public FundTransactionDto getTransaction(Long transactionId) {
        try {
            String url = FUND_SERVICE_URL + "/" + transactionId;
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<FundTransactionDto>>() {}
            );
            ApiResponse<FundTransactionDto> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Fund service unavailable: {}", e.getMessage());
            throw new RuntimeException("Fund service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Error fetching transaction {}: {}", transactionId, e.getMessage());
            return null;
        }
    }

    public void approveTransaction(Long transactionId, String approvedBy) {
        try {
            String url = FUND_SERVICE_URL + "/" + transactionId + "/approve?approvedBy=" + approvedBy;
            restTemplate.put(url, null);
        } catch (ResourceAccessException e) {
            log.error("Fund service unavailable: {}", e.getMessage());
            throw new RuntimeException("Fund service is unavailable. Please try again later.");
        }
    }

    public void rejectTransaction(Long transactionId, String rejectedBy, String reason) {
        try {
            String url = FUND_SERVICE_URL + "/" + transactionId + "/reject?approvedBy=" + rejectedBy + "&reason=" + reason;
            restTemplate.put(url, null);
        } catch (ResourceAccessException e) {
            log.error("Fund service unavailable: {}", e.getMessage());
            throw new RuntimeException("Fund service is unavailable. Please try again later.");
        }
    }
}
