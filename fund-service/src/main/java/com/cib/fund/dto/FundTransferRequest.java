package com.cib.fund.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransferRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Beneficiary ID is required")
    private Long beneficiaryId;

    @NotBlank(message = "Beneficiary account is required")
    private String beneficiaryAccount;

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Initiated by is required")
    private String initiatedBy;
}
