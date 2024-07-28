package dev.carrynong.goutbackend.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopupDTO(
    @DecimalMin(value = "0.0", inclusive = false)
    @NotNull BigDecimal amount,
    Integer userId,
    String idempotentKey
) {
}
