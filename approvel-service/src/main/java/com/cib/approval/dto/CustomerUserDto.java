package com.cib.approval.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerUserDto {
    private Long id;
    private String employeeName;
    private String email;
    private String role;
    private String checkerLevel;
    private String status;
}
