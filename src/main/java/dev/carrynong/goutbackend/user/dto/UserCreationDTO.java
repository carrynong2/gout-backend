package dev.carrynong.goutbackend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
         String phoneNumber,
        @NotBlank String email,
        @NotBlank String password
) {
}
