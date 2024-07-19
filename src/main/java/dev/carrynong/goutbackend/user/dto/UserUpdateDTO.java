package dev.carrynong.goutbackend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}
