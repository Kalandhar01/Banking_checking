package com.cib.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionRequest {

    @NotBlank(message = "Checker ID is required")
    private String checkerId;

    @NotBlank(message = "Action is required (ACCEPT or REJECT)")
    private String action;

    private String remarks;
}
