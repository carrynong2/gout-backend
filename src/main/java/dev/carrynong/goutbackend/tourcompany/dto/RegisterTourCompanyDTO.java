package dev.carrynong.goutbackend.tourcompany.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterTourCompanyDTO(Integer id,
                                     @NotBlank String name,
                                     @NotBlank String username,
                                     @NotBlank String password,
                                     String status) {
}
