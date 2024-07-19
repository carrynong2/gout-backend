package dev.carrynong.goutbackend.auth.controller;

import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LoginResponseDTO;
import dev.carrynong.goutbackend.auth.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Validated LoginRequestDTO body) {
        return authService.login(body);
    }

}
