package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LoginResponseDTO;
import dev.carrynong.goutbackend.auth.dto.LogoutDTO;
import dev.carrynong.goutbackend.auth.dto.RefreshTokenDTO;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {
    Optional<UserLogin> findCredentialByUsername(String email);
    Optional<UserLogin> findCredentialByUserId(int userId);
    UserLogin createConsumerCredential(int userId, String email, String password);
    void deleteCredentialByUserId(int userId);
    LoginResponseDTO login(LoginRequestDTO body);
    LoginResponseDTO issueNewAccessToken(RefreshTokenDTO body);
    void logout(Authentication authentication);
    void logout(LogoutDTO logoutDTO);
}
