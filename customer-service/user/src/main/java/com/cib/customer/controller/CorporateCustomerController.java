package com.cib.customer.controller;


import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateCustomerRequest;
import com.cib.customer.dto.CorporateCustomerResponse;
import com.cib.customer.service.CorporateCustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/company")
public class CorporateCustomerController {

    private final CorporateCustomerService customerService;

    public CorporateCustomerController(CorporateCustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CorporateCustomerResponse>> createCustomer(
            @Valid @RequestBody CorporateCustomerRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.createCustomer(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CorporateCustomerResponse>>> getAllCustomers() {

        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CorporateCustomerResponse>> getCustomerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CorporateCustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CorporateCustomerRequest request) {

        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long id) {

        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }


}
