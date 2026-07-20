package com.cib.fund.dto;

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
    private String checkerLevel;
    private String status;
}
