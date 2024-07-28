package dev.carrynong.goutbackend.auth;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LogoutDTO;
import dev.carrynong.goutbackend.auth.dto.RefreshTokenDTO;
import dev.carrynong.goutbackend.auth.model.RefreshToken;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.RefreshTokenRepository;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.auth.service.AuthServiceImpl;
import dev.carrynong.goutbackend.auth.service.TokenService;
import dev.carrynong.goutbackend.common.Constants;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyLoginRepository;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    @Spy
    private AuthServiceImpl authService;
    @Mock
    private UserLoginRepository userLoginRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TourCompanyLoginRepository tourCompanyLoginRepository;


    @Test
    void whenFindByUserNameThenSuccess() {
        var email = "test@test.com";
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserLogin = new UserLogin(1, userReference,email , "*&*^*())(*)(");
        when(userLoginRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockUserLogin));
        var actual = authService.findCredentialByUsername(email);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(1, actual.get().id().intValue());
        Assertions.assertEquals(userReference, actual.get().userId());
        Assertions.assertEquals(email, actual.get().email());
        Assertions.assertEquals("*&*^*())(*)(", actual.get().password());
    }

    @Test
    void findByUserIdThenSuccess() {
        var email = "test@test.com";
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserLogin = new UserLogin(1, userReference,email , "*&*^*())(*)(");
        when(userLoginRepository.findByUserId(any(AggregateReference.class)))
                .thenReturn(Optional.of(mockUserLogin));
        var actual = authService.findCredentialByUserId(1);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(1, actual.get().id().intValue());
        Assertions.assertEquals(userReference, actual.get().userId());
        Assertions.assertEquals(email, actual.get().email());
        Assertions.assertEquals("*&*^*())(*)(", actual.get().password());
    }

    @Test
    void whenCreateCredentialThenSuccess() {
        var mockEncryptedPassword = "*&*^*())(*)(";
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);

        when(passwordEncoder.encode(anyString()))
                .thenReturn(mockEncryptedPassword);
        var mockUserCredential = new UserLogin(1, userReference, "test@test.com", mockEncryptedPassword);
        when(userLoginRepository.save(any(UserLogin.class)))
                .thenReturn(mockUserCredential);
        var actual = authService.createConsumerCredential(1, "test@test.com", mockEncryptedPassword);

        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals(userReference, actual.userId());
        Assertions.assertEquals("test@test.com", actual.email());
        Assertions.assertEquals("*&*^*())(*)(", actual.password());
    }

    @Test
    void whenDeleteCredentialThenSuccess() {
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserLogin = new UserLogin(1, userReference,"test@test.com" , "*&*^*())(*)(");
        when(authService.findCredentialByUserId(anyInt()))
                .thenReturn(Optional.of(mockUserLogin));
        doNothing().when(userLoginRepository).delete(any(UserLogin.class));
        Assertions.assertDoesNotThrow(() ->  authService.deleteCredentialByUserId(1));
    }

    @Test
    void whenDeleteCredentialButNotFoundThenFail() {
        when(authService.findCredentialByUserId(anyInt()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> authService.deleteCredentialByUserId(1));
    }

    // Tests for login
    @Test
    void whenLoginSuccessfulThenReturnTokens() {
        var username = "test@test.com";
        var password = "password";
        var userId = 1;
        var accessToken = "accessToken";
        var refreshToken = "refreshToken";
        var role = RoleEnum.CONSUMER.name();

        var loginRequestDTO = new LoginRequestDTO(username, password);
        var authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        var authentication = mock(Authentication.class);
        var authenticateUser = mock(AuthenticateUser.class);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticateUser);
        when(authenticateUser.userId()).thenReturn(userId);
        when(authenticateUser.role()).thenReturn(RoleEnum.valueOf(role));
        when(tokenService.issueAccessToken(any(Authentication.class), any(Instant.class))).thenReturn(accessToken);
        when(tokenService.issueRefreshToken()).thenReturn(refreshToken);

        var loginResponse = authService.login(loginRequestDTO);

        Assertions.assertEquals(userId, loginResponse.userId());
        Assertions.assertEquals(Constants.TOKEN_TYPE, loginResponse.tokenType());
        Assertions.assertEquals(accessToken, loginResponse.accessToken());
        Assertions.assertEquals(refreshToken, loginResponse.refreshToken());
    }

    @Test
    void whenIssueNewAccessTokenSuccessfulThenReturnNewTokens() {
        // Given
        var userId = 2;
        var oldRefreshToken = "oldRefreshToken";
        var newAccessToken = "newAccessToken";
        var newRefreshToken = "newRefreshToken";
        var usage = RoleEnum.CONSUMER.name(); // Or "USER" based on your enum

        var refreshTokenDTO = new RefreshTokenDTO(usage, userId, oldRefreshToken);
        var refreshTokenEntity = new RefreshToken(null, oldRefreshToken, Instant.now(), usage, userId, false);

        var user = new User(userId, "user@example.com", "password", "063-904-2389"); // Adjust User constructor as needed
        var userCredential = new AuthenticateUser(userId, "user@example.com", "password", RoleEnum.CONSUMER); // Adjust as needed

        // Mocking the necessary repository methods
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshTokenEntity));
        when(tokenService.isRefreshTokenExpired(any(RefreshToken.class))).thenReturn(false);
        when(tokenService.issueAccessToken(any(Authentication.class), any(Instant.class))).thenReturn(newAccessToken);
        when(tokenService.rotateRefreshTokenIfNeed(any())).thenReturn(newRefreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        var loginResponse = authService.issueNewAccessToken(refreshTokenDTO);

        // Then
        Assertions.assertEquals(userId, loginResponse.userId());
        Assertions.assertEquals(Constants.TOKEN_TYPE, loginResponse.tokenType());
        Assertions.assertEquals(newAccessToken, loginResponse.accessToken());
        Assertions.assertEquals(newRefreshToken, loginResponse.refreshToken());
    }
    // Tests for logout (using Authentication)
    @Test
    void whenLogoutSuccessfulThenTokensInvalidated() {
        var userId = 1;
        var role = RoleEnum.CONSUMER.name();

        var authentication = mock(Authentication.class);
        var authenticateUser = mock(AuthenticateUser.class);

        when(authentication.getPrincipal()).thenReturn(authenticateUser);
        when(authenticateUser.userId()).thenReturn(userId);
        when(authenticateUser.role()).thenReturn(RoleEnum.valueOf(role));

        authService.logout(authentication);

        verify(refreshTokenRepository, times(1))
                .updateRefreshTokenByResource(role, userId, true);
    }

    // Tests for logout (using LogoutDTO)
    @Test
    void whenLogoutUsingLogoutDTOThenTokensInvalidated() {
        var userId = "1";
        var role = RoleEnum.CONSUMER.name();

        var logoutDTO = new LogoutDTO(userId, role);

        authService.logout(logoutDTO);

        verify(refreshTokenRepository, times(1))
                .updateRefreshTokenByResource(role, Integer.parseInt(userId), true);
    }
}
