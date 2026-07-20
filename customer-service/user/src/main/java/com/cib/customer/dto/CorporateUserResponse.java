package com.cib.customer.dto;

import com.cib.customer.enums.AccountType;
import com.cib.customer.enums.Role;
import com.cib.customer.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CorporateUserResponse {

    private Long id;

    private String employeeName;

    private String email;

    private Role role;

    private Status status;

    private Long customerId;

    private String companyName;
    private String accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

}
