package dev.carrynong.goutbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenDTO(
        @NotBlank String usage,
        @NotNull Integer resourceId,
        @NotBlank String refreshToken
) {
}
