package com.cib.customer.service;

import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateCustomerRequest;
import com.cib.customer.dto.CorporateCustomerResponse;

import java.util.List;

public interface CorporateCustomerService {

    ApiResponse<CorporateCustomerResponse> createCustomer(CorporateCustomerRequest request);

    ApiResponse<List<CorporateCustomerResponse>> getAllCustomers();

    ApiResponse<CorporateCustomerResponse> getCustomerById(Long id);

    ApiResponse<CorporateCustomerResponse> updateCustomer(Long id,
                                                          CorporateCustomerRequest request);

    ApiResponse<Void> deleteCustomer(Long id);
}
