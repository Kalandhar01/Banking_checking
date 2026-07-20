package com.cib.fund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequest {
    @NotBlank
    private String checkerId;
}
