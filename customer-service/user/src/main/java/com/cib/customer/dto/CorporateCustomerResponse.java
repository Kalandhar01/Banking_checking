package com.cib.customer.dto;


import com.cib.customer.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CorporateCustomerResponse {

    private Long id;

    private String companyName;

    private String companyCode;

    private String address;

    private Status status;
}
