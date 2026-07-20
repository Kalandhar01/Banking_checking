package com.cib.customer.dto;

import com.cib.customer.enums.AccountType;
import com.cib.customer.enums.Status;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private Status status;
}
