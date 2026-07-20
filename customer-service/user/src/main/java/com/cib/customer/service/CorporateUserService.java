package com.cib.customer.service;

import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateUserRequest;
import com.cib.customer.dto.CorporateUserResponse;

import java.util.List;

public interface CorporateUserService {

    ApiResponse<CorporateUserResponse> createUser(CorporateUserRequest request);

    ApiResponse<List<CorporateUserResponse>> getAllUsers();

    ApiResponse<CorporateUserResponse> getUserById(Long id);

    ApiResponse<CorporateUserResponse> updateUser(Long id,
                                                  CorporateUserRequest request);

    ApiResponse<Void> deleteUser(Long id);
}
