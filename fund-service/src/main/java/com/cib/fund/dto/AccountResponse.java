package com.cib.fund.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private boolean success;
    private String message;
    private AccountData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountData {
        private Long id;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private String status;
    }
}
