package com.cib.approval.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransactionRequest {
    private BigDecimal amount;
    private String reference;
}
