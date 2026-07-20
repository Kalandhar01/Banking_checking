package com.cib.customer.service.impl;

import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateUserRequest;
import com.cib.customer.dto.CorporateUserResponse;
import com.cib.customer.entity.CorporateCustomer;
import com.cib.customer.entity.CorporateUser;
import com.cib.customer.exception.ResourceNotFoundException;
import com.cib.customer.exception.UserAlreadyExistsException;
import com.cib.customer.mapper.CorporateUserMapper;
import com.cib.customer.repository.CorporateAccountRepository;
import com.cib.customer.repository.CorporateCustomerRepository;
import com.cib.customer.repository.CorporateUserRepository;
import com.cib.customer.service.CorporateUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorporateUserServiceImpl implements CorporateUserService {

    private final CorporateUserRepository userRepository;
    private final CorporateCustomerRepository customerRepository;
    private final CorporateAccountRepository accountRepository;

    @Override
    @Transactional
    public ApiResponse<CorporateUserResponse> createUser(CorporateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email : " + request.getEmail());
        }

        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new UserAlreadyExistsException(
                    "Account already exists with account number : "
                            + request.getAccountNumber());
        }

        CorporateCustomer customer = getCustomer(request.getCustomerId());

        CorporateUser user = CorporateUserMapper.toEntity(request, customer);

        CorporateUser savedUser = userRepository.save(user);

        return ApiResponse.<CorporateUserResponse>builder()
                .success(true)
                .message("User '" + savedUser.getEmployeeName() + "' created successfully with ID: " + savedUser.getId())
                .data(CorporateUserMapper.toResponse(savedUser))
                .build();
    }

    @Override
    public ApiResponse<List<CorporateUserResponse>> getAllUsers() {

        List<CorporateUserResponse> users = userRepository.findAll()
                .stream()
                .map(CorporateUserMapper::toResponse)
                .toList();

        return ApiResponse.<List<CorporateUserResponse>>builder()
                .success(true)
                .message(users.isEmpty()
                        ? "No users found."
                        : "Users fetched successfully. Count: " + users.size())
                .data(users)
                .build();
    }

    @Override
    public ApiResponse<CorporateUserResponse> getUserById(Long id) {

        CorporateUser user = getUser(id);

        return ApiResponse.<CorporateUserResponse>builder()
                .success(true)
                .message("User '" + user.getEmployeeName() + "' fetched successfully")
                .data(CorporateUserMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<CorporateUserResponse> updateUser(Long id,
                                                         CorporateUserRequest request) {

        CorporateUser user = getUser(id);

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new UserAlreadyExistsException(
                    "User already exists with email : " + request.getEmail());
        }

        if (!user.getCorporateAccount().getAccountNumber()
                .equals(request.getAccountNumber())
                && accountRepository.existsByAccountNumber(request.getAccountNumber())) {

            throw new UserAlreadyExistsException(
                    "Account already exists with account number : "
                            + request.getAccountNumber());
        }

        CorporateCustomer customer = getCustomer(request.getCustomerId());

        CorporateUserMapper.updateEntity(user, request, customer);

        CorporateUser updatedUser = userRepository.save(user);

        return ApiResponse.<CorporateUserResponse>builder()
                .success(true)
                .message("User '" + updatedUser.getEmployeeName() + "' updated successfully")
                .data(CorporateUserMapper.toResponse(updatedUser))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {

        CorporateUser user = getUser(id);

        userRepository.delete(user);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("User '" + user.getEmployeeName() + "' deleted successfully")
                .build();
    }

    private CorporateUser getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));
    }

    private CorporateCustomer getCustomer(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + id));
    }
}
