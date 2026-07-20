package com.cib.fund.dto;

import com.cib.fund.enums.TransactionStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAuditResponse {
    private Long id;
    private Long transactionId;
    private TransactionStatus fromStatus;
    private TransactionStatus toStatus;
    private String changedBy;
    private String comment;
    private LocalDateTime createdAt;
}
