package com.example.assignment.point.wallet.dto.response;

import com.example.assignment.point.wallet.domain.entity.Wallet;
import jakarta.validation.constraints.NotNull;

public record ChargeWalletResponse(
    @NotNull
    Long walletId,

    @NotNull
    Long balance
) {

    public static ChargeWalletResponse fromEntity(Wallet wallet) {
        return new ChargeWalletResponse(wallet.getId(), wallet.getBalance());
    }
}
