package dev.carrynong.goutbackend.wallet.dto;

import java.math.BigDecimal;

public record TourCompanyWalletInfoDTO(
        Integer resourceId,
        BigDecimal balance
) {
}
