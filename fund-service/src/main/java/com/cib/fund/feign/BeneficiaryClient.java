package com.cib.fund.feign;

import com.cib.fund.dto.BeneficiaryValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryClient {

    private final RestTemplate restTemplate;

    public BeneficiaryValidationResponse validateBeneficiary(Long beneficiaryId) {
        try {
            String url = "http://beneficiary-service/beneficiary/" + beneficiaryId + "/validate";
            return restTemplate.getForObject(url, BeneficiaryValidationResponse.class);
        } catch (ResourceAccessException e) {
            log.error("Beneficiary service is unavailable: {}", e.getMessage());
            return BeneficiaryValidationResponse.builder()
                    .success(false)
                    .message("Beneficiary service is unavailable. Please try again later.")
                    .data(false)
                    .build();
        } catch (Exception e) {
            log.error("Error validating beneficiary {}: {}", beneficiaryId, e.getMessage());
            return BeneficiaryValidationResponse.builder()
                    .success(false)
                    .message("Failed to validate beneficiary: " + e.getMessage())
                    .data(false)
                    .build();
        }
    }
}
