package com.cib.fund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckerActionRequest {

    @NotBlank(message = "Checker ID is required")
    private String checkerId;

    @NotBlank(message = "Action is required (ACCEPT or REJECT)")
    private String action;

    private String remarks;
}
