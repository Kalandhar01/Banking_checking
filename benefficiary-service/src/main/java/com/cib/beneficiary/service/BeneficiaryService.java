package com.cib.beneficiary.service;

import com.cib.beneficiary.dto.BeneficiaryRequest;
import com.cib.beneficiary.dto.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryResponse addBeneficiary(BeneficiaryRequest request);
    BeneficiaryResponse updateBeneficiary(Long id, BeneficiaryRequest request);
    void deleteBeneficiary(Long id);
    BeneficiaryResponse getBeneficiaryById(Long id);
    List<BeneficiaryResponse> getAllBeneficiaries();
    List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId);
    boolean validateBeneficiary(Long beneficiaryId);
}
