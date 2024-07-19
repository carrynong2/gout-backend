package dev.carrynong.goutbackend.user.dto;

public record UserInfoDTO(
        Integer id,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
