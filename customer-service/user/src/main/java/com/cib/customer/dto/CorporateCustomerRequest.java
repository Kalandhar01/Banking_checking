package com.cib.customer.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CorporateCustomerRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company code is required")
    private String companyCode;

    @NotBlank(message = "Address is required")
    private String address;
}
