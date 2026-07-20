package com.cib.fund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FailRequest {
    @NotBlank
    private String approvedBy;
    @NotBlank
    private String reason;
}
