package com.cib.approval.mapper;

import com.cib.approval.dto.ApprovalResponse;
import com.cib.approval.entity.ApprovalRequest;
import org.springframework.stereotype.Component;

@Component
public class ApprovalMapper {

    public ApprovalResponse toResponse(ApprovalRequest approval) {
        return ApprovalResponse.builder()
                .id(approval.getId())
                .transactionId(approval.getTransactionId())
                .makerId(approval.getMakerId())
                .makerName(approval.getMakerName())
                .checkerId(approval.getCheckerId())
                .level2CheckerId(approval.getLevel2CheckerId())
                .status(approval.getStatus())
                .comments(approval.getComments())
                .createdAt(approval.getCreatedAt())
                .updatedAt(approval.getUpdatedAt())
                .build();
    }
}
