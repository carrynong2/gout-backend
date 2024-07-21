package dev.carrynong.goutbackend.auth.controller;

import dev.carrynong.goutbackend.auth.dto.*;
import dev.carrynong.goutbackend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Validated LoginRequestDTO body) {
        return ResponseEntity.ok(authService.login(body));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody @Validated RefreshTokenDTO body) {
        return ResponseEntity.ok(authService.issueNewAccessToken(body));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        var jwt = (Jwt) authentication.getPrincipal();
        var logoutDto = new LogoutDTO(jwt.getClaimAsString("sub"), jwt.getClaimAsString("roles"));
        authService.logout(logoutDto);
        return ResponseEntity.noContent().build();
    }

}
