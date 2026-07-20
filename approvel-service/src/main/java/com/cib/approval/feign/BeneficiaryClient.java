package com.cib.approval.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryClient {

    private final RestTemplate restTemplate;

    public boolean isBeneficiaryActive(Long beneficiaryId) {
        try {
            String url = "http://beneficiary-service/beneficiary/" + beneficiaryId + "/validate";
            @SuppressWarnings("rawtypes")
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                Object data = response.get("data");
                return data != null && Boolean.TRUE.toString().equalsIgnoreCase(data.toString());
            }
            return false;
        } catch (ResourceAccessException e) {
            log.error("Beneficiary service unavailable: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating beneficiary {}: {}", beneficiaryId, e.getMessage());
            return false;
        }
    }
}
