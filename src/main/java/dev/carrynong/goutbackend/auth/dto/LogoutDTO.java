package dev.carrynong.goutbackend.auth.dto;

public record LogoutDTO(
        String sub,
        String roles
) {
}
