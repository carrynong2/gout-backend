package dev.carrynong.goutbackend.wallet.dto;

import java.math.BigDecimal;

public record UserWalletInfoDTO(
        Integer userId,
        BigDecimal balance
) {
}
