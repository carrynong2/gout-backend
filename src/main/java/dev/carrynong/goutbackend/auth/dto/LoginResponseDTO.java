package dev.carrynong.goutbackend.auth.dto;

public record LoginResponseDTO(
        Integer userId,
        String tokenType,
        String accessToken,
        String refreshToken
) {
}
