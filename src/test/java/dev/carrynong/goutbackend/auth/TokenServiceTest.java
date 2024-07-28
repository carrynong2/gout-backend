package dev.carrynong.goutbackend.auth;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.model.RefreshToken;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.RefreshTokenRepository;
import dev.carrynong.goutbackend.auth.service.CustomUserDetailService;
import dev.carrynong.goutbackend.auth.service.TokenService;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private CustomUserDetailService customUserDetailService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIssueAccessTokenWithAuthentication() {
        // Given
        Authentication authentication = mock(Authentication.class);
        AuthenticateUser userDetails = new AuthenticateUser(1, "user@example.com", "password", RoleEnum.CONSUMER);
        Instant now = Instant.now();
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String expectedToken = "encodedToken";
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gout-backend")
                .issuedAt(now)
                .subject("1")
                .claim("roles", "ROLE_USER")
                .expiresAt(now.plusSeconds(3600))
                .build();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(expectedToken);

        // Mock JwtEncoder to return a Jwt object
        when(jwtEncoder.encode(JwtEncoderParameters.from(claims))).thenReturn(jwt);

        // When
        String token = tokenService.issueAccessToken(authentication, now);

        // Then
        assertEquals(expectedToken, token);
    }

    @Test
    void testIssueAccessTokenWithUserLogin() {
        // Given
        AggregateReference<User, Integer> userId = AggregateReference.to(1);
        UserLogin userLogin = new UserLogin(null, userId, "user@example.com", "password");
        AuthenticateUser userDetails = new AuthenticateUser(1, "user@example.com", "password", RoleEnum.CONSUMER);
        Instant now = Instant.now();
        when(customUserDetailService.loadUserByUsername(userLogin.email())).thenReturn(userDetails);

        String expectedToken = "encodedToken";
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gout-backend")
                .issuedAt(now)
                .subject("1")
                .claim("roles", "ROLE_USER")
                .expiresAt(now.plusSeconds(3600))
                .build();

        // Mock Jwt object
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(expectedToken);

        // Mock JwtEncoder to return the Jwt object
        when(jwtEncoder.encode(JwtEncoderParameters.from(claims))).thenReturn(jwt);

        // When
        String token = tokenService.issueAccessToken(userLogin, now);

        // Then
        assertEquals(expectedToken, token);
    }

    @Test
    void testIssueRefreshToken() {
        // Given
        String refreshToken = UUID.randomUUID().toString();

        // When
        String token = tokenService.issueRefreshToken();

        // Then
        assertNotNull(token);
        assertNotEquals(refreshToken, token); // Ensure that different tokens are generated
    }

    @Test
    void testIsRefreshTokenExpired() {
        // Given
        Instant now = Instant.now();
        RefreshToken expiredToken = new RefreshToken(
                1, "token", now.minusSeconds(86400), "usage", 1, false
        );

        // When
        boolean isExpired = tokenService.isRefreshTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void testRotateRefreshTokenIfNeed() {
        // Given
        Instant now = Instant.now();
        RefreshToken refreshToken = new RefreshToken(
                1, "oldToken", now.minusSeconds(600), "usage", 1, false
        );
        String newToken = "newToken";
        when(tokenService.issueRefreshToken()).thenReturn(newToken);

        // When
        String token = tokenService.rotateRefreshTokenIfNeed(refreshToken);

        // Then
        assertEquals(newToken, token);
    }

    @Test
    void testCleanupRefreshTokenThatNotExpired() {
        // Given
        Instant now = Instant.now();
        // The tokens are not used in this test; they are only used to set up the scenario
        RefreshToken expiredToken = new RefreshToken(1, "token", now.minusSeconds(86400), "usage", 1, false); // 1 day ago
        RefreshToken notExpiredToken = new RefreshToken(2, "token", now.minusSeconds(3600), "usage", 1, false); // 1 hour ago

        // No need to define the return value, as we are testing a void method
        // just ensure the method call occurs
        doNothing().when(refreshTokenRepository).updateRefreshTokenThatExpired(anyBoolean(), any(Instant.class));

        // When
        tokenService.cleanupRefreshTokenThatNotExpired();

        // Then
        verify(refreshTokenRepository, times(1)).updateRefreshTokenThatExpired(true, now.minusSeconds(86400));
    }
}
