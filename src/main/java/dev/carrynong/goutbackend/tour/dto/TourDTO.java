package dev.carrynong.goutbackend.tour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TourDTO(
        @NotNull Integer tourCompanyId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        @NotNull Integer numberOfPeople,
        @NotNull Instant activityDate,
        String status) {
}
