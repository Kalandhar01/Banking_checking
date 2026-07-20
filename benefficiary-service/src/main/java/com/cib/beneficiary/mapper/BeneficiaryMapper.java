package com.cib.beneficiary.mapper;

import com.cib.beneficiary.dto.BeneficiaryResponse;
import com.cib.beneficiary.entity.Beneficiary;

public final class BeneficiaryMapper {

    private BeneficiaryMapper() {
    }

    public static BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .customerId(beneficiary.getCustomerId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .accountNumber(beneficiary.getAccountNumber())
                .bankName(beneficiary.getBankName())
                .ifscCode(beneficiary.getIfscCode())
                .status(beneficiary.getStatus())
                .createdAt(beneficiary.getCreatedAt())
                .updatedAt(beneficiary.getUpdatedAt())
                .build();
    }
}
