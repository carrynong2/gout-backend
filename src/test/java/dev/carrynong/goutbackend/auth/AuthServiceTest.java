package dev.carrynong.goutbackend.auth;

import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.auth.service.AuthServiceImpl;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    @Spy
    private AuthServiceImpl authService;
    @Mock
    private UserLoginRepository userLoginRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

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
}
