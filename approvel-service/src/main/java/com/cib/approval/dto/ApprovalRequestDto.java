package com.cib.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestDto {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotBlank(message = "Maker ID is required")
    private String makerId;

    private String makerName;

    @NotBlank(message = "Checker ID is required")
    private String checkerId;
}
