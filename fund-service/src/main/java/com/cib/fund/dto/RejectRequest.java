package com.cib.fund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequest {
    @NotBlank
    private String rejectedBy;
    @NotBlank
    private String reason;
}
