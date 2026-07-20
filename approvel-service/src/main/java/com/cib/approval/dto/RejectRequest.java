package com.cib.approval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequest {

    @NotBlank(message = "Remarks are required")
    private String remarks;
}
