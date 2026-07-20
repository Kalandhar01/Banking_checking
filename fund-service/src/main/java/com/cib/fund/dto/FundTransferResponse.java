package com.cib.fund.dto;

import com.cib.fund.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransferResponse {
    private Long id;
    private Long customerId;
    private Long beneficiaryId;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private BigDecimal amount;
    private String currency;
    private String referenceNumber;
    private TransactionStatus status;
    private String initiatedBy;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
