package com.cib.approval.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorporateCustomerResponse {
    private Long id;
    private String companyName;
    private String companyCode;
    private String address;
    private String status;
}
