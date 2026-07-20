package com.cib.customer.mapper;

import com.cib.customer.dto.CorporateUserRequest;
import com.cib.customer.dto.CorporateUserResponse;
import com.cib.customer.entity.CorporateAccount;
import com.cib.customer.entity.CorporateCustomer;
import com.cib.customer.entity.CorporateUser;
import com.cib.customer.enums.Status;

public class CorporateUserMapper {

    public static CorporateUser toEntity(CorporateUserRequest request,
                                         CorporateCustomer customer) {

        CorporateAccount account = CorporateAccount.builder()
                .accountNumber(request.getAccountNumber())
                .accountType(request.getAccountType())
                .balance(request.getBalance())
                .status(Status.ACTIVE)
                .build();

        return CorporateUser.builder()
                .employeeName(request.getEmployeeName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .checkerLevel(request.getCheckerLevel())
                .status(Status.ACTIVE)
                .corporateCustomer(customer)
                .corporateAccount(account)
                .build();
    }

    public static CorporateUserResponse toResponse(CorporateUser user) {

        return CorporateUserResponse.builder()
                .id(user.getId())
                .employeeName(user.getEmployeeName())
                .email(user.getEmail())
                .role(user.getRole())
                .checkerLevel(user.getCheckerLevel())
                .status(user.getStatus())
                .customerId(user.getCorporateCustomer().getId())
                .companyName(user.getCorporateCustomer().getCompanyName())

                // Account Details
                .accountNumber(user.getCorporateAccount().getAccountNumber())
                .accountType(user.getCorporateAccount().getAccountType())
                .balance(user.getCorporateAccount().getBalance())

                .build();
    }

    public static void updateEntity(CorporateUser user,
                                    CorporateUserRequest request,
                                    CorporateCustomer customer) {

        user.setEmployeeName(request.getEmployeeName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setCheckerLevel(request.getCheckerLevel());
        user.setCorporateCustomer(customer);

        user.getCorporateAccount().setAccountNumber(request.getAccountNumber());
        user.getCorporateAccount().setAccountType(request.getAccountType());
        user.getCorporateAccount().setBalance(request.getBalance());
    }
}
