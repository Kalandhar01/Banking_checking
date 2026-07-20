package com.cib.approval.dto;

import com.cib.approval.enums.ApprovalStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponse {
    private Long id;
    private Long transactionId;
    private String makerId;
    private String makerName;
    private String checkerId;
    private String level2CheckerId;
    private ApprovalStatus status;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
