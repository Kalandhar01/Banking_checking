package com.cib.fund.mapper;

import com.cib.fund.dto.FundTransferResponse;
import com.cib.fund.dto.TransactionAuditResponse;
import com.cib.fund.entity.FundTransaction;
import com.cib.fund.entity.TransactionAudit;
import org.springframework.stereotype.Component;

@Component
public class FundTransactionMapper {

    public FundTransferResponse toResponse(FundTransaction txn) {
        return FundTransferResponse.builder()
                .id(txn.getId())
                .customerId(txn.getCustomerId())
                .beneficiaryId(txn.getBeneficiaryId())
                .beneficiaryAccount(txn.getBeneficiaryAccount())
                .beneficiaryName(txn.getBeneficiaryName())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .referenceNumber(txn.getReferenceNumber())
                .status(txn.getStatus())
                .initiatedBy(txn.getInitiatedBy())
                .approvedBy(txn.getApprovedBy())
                .rejectionReason(txn.getRejectionReason())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }

    public TransactionAuditResponse toAuditResponse(TransactionAudit audit) {
        return TransactionAuditResponse.builder()
                .id(audit.getId())
                .transactionId(audit.getTransactionId())
                .fromStatus(audit.getFromStatus())
                .toStatus(audit.getToStatus())
                .changedBy(audit.getChangedBy())
                .comment(audit.getComment())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}
