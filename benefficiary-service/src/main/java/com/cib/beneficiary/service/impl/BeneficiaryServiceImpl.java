package com.cib.beneficiary.service.impl;

import com.cib.beneficiary.dto.BeneficiaryRequest;
import com.cib.beneficiary.dto.BeneficiaryResponse;
import com.cib.beneficiary.entity.Beneficiary;
import com.cib.beneficiary.enums.Status;
import com.cib.beneficiary.exception.BeneficiaryAlreadyExistsException;
import com.cib.beneficiary.exception.ResourceNotFoundException;
import com.cib.beneficiary.mapper.BeneficiaryMapper;
import com.cib.beneficiary.repository.BeneficiaryRepository;
import com.cib.beneficiary.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    @Transactional
    public BeneficiaryResponse addBeneficiary(BeneficiaryRequest request) {
        if (beneficiaryRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new BeneficiaryAlreadyExistsException(
                    "Beneficiary with account number " + request.getAccountNumber() + " already exists");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .customerId(request.getCustomerId())
                .beneficiaryName(request.getBeneficiaryName())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .status(Status.ACTIVE)
                .build();

        beneficiary = beneficiaryRepository.save(beneficiary);
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiary(Long id, BeneficiaryRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));

        beneficiaryRepository.findByAccountNumber(request.getAccountNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BeneficiaryAlreadyExistsException(
                                "Beneficiary with account number " + request.getAccountNumber() + " already exists");
                    }
                });

        beneficiary.setCustomerId(request.getCustomerId());
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setAccountNumber(request.getAccountNumber());
        beneficiary.setBankName(request.getBankName());
        beneficiary.setIfscCode(request.getIfscCode());

        beneficiary = beneficiaryRepository.save(beneficiary);
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long id) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));
        beneficiary.setStatus(Status.INACTIVE);
        beneficiaryRepository.save(beneficiary);
    }

    @Override
    public BeneficiaryResponse getBeneficiaryById(Long id) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + id));
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    public List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId) {
        return beneficiaryRepository.findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryMapper::toResponse)
                .toList();
    }

    @Override
    public boolean validateBeneficiary(Long beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(b -> b.getStatus() == Status.ACTIVE)
                .isPresent();
    }
}
