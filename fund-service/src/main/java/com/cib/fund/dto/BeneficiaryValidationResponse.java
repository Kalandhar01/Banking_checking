package com.cib.fund.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryValidationResponse {
    private boolean success;
    private String message;
    private Boolean data;
}
