package com.cib.approval.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionDto {
    private Long id;
    private Long userId;
    private Long customerId;
    private Long beneficiaryId;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private BigDecimal amount;
    private String currency;
    private String referenceNumber;
    private String status;
    private String initiatedBy;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
