package com.cib.fund.feign;

import com.cib.fund.dto.ApiResponse;
import com.cib.fund.dto.ApprovalRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalServiceClient {

    private final RestTemplate restTemplate;
    private static final String APPROVAL_SERVICE_URL = "http://approval-service/approval";

    public void submitForApproval(Long transactionId, String makerId) {
        try {
            String url = APPROVAL_SERVICE_URL + "/submit";
            ApprovalRequestDto request = ApprovalRequestDto.builder()
                    .transactionId(transactionId)
                    .makerId(makerId)
                    .build();
            HttpEntity<ApprovalRequestDto> entity = new HttpEntity<>(request);
            restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ApiResponse<Object>>() {});
            log.info("Auto-submitted approval for transaction {} by maker {}", transactionId, makerId);
        } catch (ResourceAccessException e) {
            log.error("Approval service unavailable: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error submitting approval for transaction {}: {}", transactionId, e.getMessage());
        }
    }
}
