package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LoginResponseDTO;
import dev.carrynong.goutbackend.auth.model.UserLogin;

import java.util.Optional;

public interface AuthService {
    Optional<UserLogin> findCredentialByUsername(String email);
    Optional<UserLogin> findCredentialByUserId(int userId);
    UserLogin createConsumerCredential(int userId, String email, String password);
    void deleteCredentialByUserId(int userId);
    LoginResponseDTO login(LoginRequestDTO body);
}
