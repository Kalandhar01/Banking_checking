package com.cib.customer.controller;


import com.cib.customer.dto.ApiResponse;
import com.cib.customer.dto.CorporateUserRequest;
import com.cib.customer.dto.CorporateUserResponse;
import com.cib.customer.service.CorporateUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/users")
@RequiredArgsConstructor
public class CorporateUserController {

    private final CorporateUserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CorporateUserResponse>> createUser(
            @Valid @RequestBody CorporateUserRequest request) {

        return new ResponseEntity<>(
                userService.createUser(request),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CorporateUserResponse>>> getAllUsers() {

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CorporateUserResponse>> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CorporateUserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CorporateUserRequest request) {

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
