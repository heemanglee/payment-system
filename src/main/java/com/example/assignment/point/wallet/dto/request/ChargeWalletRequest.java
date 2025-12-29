package com.example.assignment.point.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChargeWalletRequest(
    @NotNull
    Long amount,

    @NotBlank
    String reason
) {
}
