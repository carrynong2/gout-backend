package dev.carrynong.goutbackend.auth.dto;

public record LoginResponseDTO(
        Integer userId,
        String token
) {
}
