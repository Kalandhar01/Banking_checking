package com.cib.customer.dto;

import com.cib.customer.enums.AccountType;
import com.cib.customer.enums.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CorporateUserRequest {

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Customer Id is required")
    private Long customerId;

    // Account Details

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "0.0", inclusive = true,
            message = "Balance cannot be negative")
    private BigDecimal balance;
}
