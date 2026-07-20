package com.cib.approval.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserDto {
    private Long id;
    private String employeeName;
    private String email;
    private String role;
    private String status;
}
