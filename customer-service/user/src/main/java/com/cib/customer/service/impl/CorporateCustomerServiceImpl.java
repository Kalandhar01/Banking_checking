package com.cib.customer.service.impl;

import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateCustomerRequest;
import com.cib.customer.dto.CorporateCustomerResponse;
import com.cib.customer.entity.CorporateCustomer;
import com.cib.customer.exception.CustomerAlreadyExistsException;
import com.cib.customer.exception.ResourceNotFoundException;
import com.cib.customer.mapper.CorporateCustomerMapper;
import com.cib.customer.repository.CorporateCustomerRepository;
import com.cib.customer.service.CorporateCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorporateCustomerServiceImpl implements CorporateCustomerService {

    private final CorporateCustomerRepository customerRepository;

    @Override
    @Transactional
    public ApiResponse<CorporateCustomerResponse> createCustomer(CorporateCustomerRequest request) {

        if (customerRepository.existsByCompanyCode(request.getCompanyCode())) {
            throw new CustomerAlreadyExistsException(
                    "Company with code '" + request.getCompanyCode() + "' already exists.");
        }

        CorporateCustomer customer = CorporateCustomerMapper.toEntity(request);

        CorporateCustomer savedCustomer = customerRepository.save(customer);

        return ApiResponse.<CorporateCustomerResponse>builder()
                .success(true)
                .message("Customer '" + savedCustomer.getCompanyName() + "' created successfully with ID: " + savedCustomer.getId())
                .data(CorporateCustomerMapper.toResponse(savedCustomer))
                .build();
    }

    @Override
    public ApiResponse<List<CorporateCustomerResponse>> getAllCustomers() {

        List<CorporateCustomerResponse> customers = customerRepository.findAll()
                .stream()
                .map(CorporateCustomerMapper::toResponse)
                .toList();

        return ApiResponse.<List<CorporateCustomerResponse>>builder()
                .success(true)
                .message(customers.isEmpty()
                        ? "No customers found."
                        : "Customers fetched successfully. Count: " + customers.size())
                .data(customers)
                .build();
    }

    @Override
    public ApiResponse<CorporateCustomerResponse> getCustomerById(Long id) {

        CorporateCustomer customer = getCustomer(id);

        return ApiResponse.<CorporateCustomerResponse>builder()
                .success(true)
                .message("Customer '" + customer.getCompanyName() + "' fetched successfully")
                .data(CorporateCustomerMapper.toResponse(customer))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<CorporateCustomerResponse> updateCustomer(Long id,
                                                                 CorporateCustomerRequest request) {

        CorporateCustomer customer = getCustomer(id);

        if (!customer.getCompanyCode().equals(request.getCompanyCode())
                && customerRepository.existsByCompanyCode(request.getCompanyCode())) {

            throw new CustomerAlreadyExistsException(
                    "Company code '" + request.getCompanyCode() + "' already exists.");
        }

        customer.setCompanyName(request.getCompanyName());
        customer.setCompanyCode(request.getCompanyCode());
        customer.setAddress(request.getAddress());

        CorporateCustomer updatedCustomer = customerRepository.save(customer);

        return ApiResponse.<CorporateCustomerResponse>builder()
                .success(true)
                .message("Customer '" + updatedCustomer.getCompanyName() + "' updated successfully")
                .data(CorporateCustomerMapper.toResponse(updatedCustomer))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCustomer(Long id) {

        CorporateCustomer customer = getCustomer(id);

        customerRepository.delete(customer);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Customer '" + customer.getCompanyName() + "' deleted successfully")
                .data(null)
                .build();
    }

    /**
     * Common method to fetch customer or throw exception.
     */
    private CorporateCustomer getCustomer(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + id));
    }
}
