package com.cib.beneficiary.controller;

import com.cib.beneficiary.dto.ApiResponse;
import com.cib.beneficiary.dto.BeneficiaryRequest;
import com.cib.beneficiary.dto.BeneficiaryResponse;
import com.cib.beneficiary.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beneficiary")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Beneficiary '" + response.getBeneficiaryName() + "' added successfully with ID: " + response.getId(),
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Beneficiary '" + response.getBeneficiaryName() + "' updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(@PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Beneficiary with ID " + id + " deactivated successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getBeneficiaryById(@PathVariable Long id) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Beneficiary '" + response.getBeneficiaryName() + "' found", response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiariesByCustomerId(
            @PathVariable Long customerId) {
        List<BeneficiaryResponse> response = beneficiaryService.getBeneficiariesByCustomerId(customerId);
        String message = response.isEmpty()
                ? "No beneficiaries found for customer ID " + customerId
                : "Beneficiaries retrieved successfully for customer ID " + customerId + ". Count: " + response.size();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateBeneficiary(@PathVariable Long id) {
        boolean valid = beneficiaryService.validateBeneficiary(id);
        String status = valid ? "ACTIVE" : "INACTIVE or NOT FOUND";
        return ResponseEntity.ok(ApiResponse.success(
                "Beneficiary ID " + id + " validation: " + status, valid));
    }
}
