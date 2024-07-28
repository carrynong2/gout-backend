package dev.carrynong.goutbackend.user;

import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.service.AuthService;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.CredentialExistsException;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.model.UserRole;
import dev.carrynong.goutbackend.user.repository.UserRepository;
import dev.carrynong.goutbackend.user.service.RoleService;
import dev.carrynong.goutbackend.user.service.UserServiceImpl;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private AuthService authService;
    @Mock
    private RoleService roleService;

    @Test
    void whenGetUserDTOByIdThenSuccess() {
        var mockUser = new User(1, "Test", "Test", "0800000001");
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(mockUser));

        var actual = userService.getUserDTOById(1);

        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals("Test", actual.firstName());
        Assertions.assertEquals("Test", actual.lastName());
        Assertions.assertEquals("0800000001", actual.phoneNumber());
    }

    @Test
    void whenGetUserDTOByIdButNotFoundThenFail() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> userService.getUserDTOById(1));
    }

    @Test
    void whenCreateUserThenSuccess() {
        var mockUser = new User(1, "Test", "Test", "0800000001");
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserLogin = new UserLogin(1, userReference,"test@test.com", "*&*^*())(*)(");
        when(authService.findCredentialByUsername(anyString()))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        when(walletService.createConsumerWallet(anyInt()))
                .thenReturn(new UserWallet(1, userReference, Instant.now(), new BigDecimal("0.00")));

        when(authService.createConsumerCredential(anyInt(), anyString(), anyString()))
                .thenReturn(mockUserLogin);

        when(roleService.bindingNewUser(anyInt(), eq(RoleEnum.CONSUMER)))
                .thenReturn(new UserRole(1,  AggregateReference.to(1),  AggregateReference.to(RoleEnum.CONSUMER.getId())));

        var body = new UserCreationDTO("Test", "Test",
                "0800125480", "test@test.com", "123456789");

        var actual = userService.createUser(body);

        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals("Test", actual.firstName());
        Assertions.assertEquals("Test", actual.lastName());
        Assertions.assertEquals("0800000001", actual.phoneNumber());
    }

    @Test
    void whenCreateUserOnExistingCredentialThenFail() {
        var email = "test@test.com";
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserLogin = new UserLogin(1, userReference,email , "*&*^*())(*)(");
        when(authService.findCredentialByUsername(anyString()))
                .thenReturn(Optional.of(mockUserLogin));

        var body = new UserCreationDTO("Test", "Test",
                "0800125480", "test@test.com", "123456789");

        Assertions.assertThrows(CredentialExistsException.class, () -> userService.createUser(body));
    }

    @Test
    void whenUpdateUserThenSuccess() {
        var mockUser = new User(1, "Test", "Test", "0800000001");
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(mockUser));

        var updatedUser = new User(mockUser.id(), "Test1", mockUser.lastName(), mockUser.phoneNumber());
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);


        var body = new UserUpdateDTO("Test1", "Test1");
        var actual = userService.updateUser(1, body);

        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals("Test1", actual.firstName());
        Assertions.assertEquals("Test", actual.lastName());
        Assertions.assertEquals("0800000001", actual.phoneNumber());
    }

    @Test
    void whenDeleteUserThenSuccess() {
        var mockUser = new User(1, "Test", "Test", "0800000001");
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(mockUser));
        doNothing().when(authService).deleteCredentialByUserId(anyInt());
        doNothing().when(walletService).deleteConsumerWalletByUserId(anyInt());
        doNothing().when(roleService).deleteRoleByUserId(anyInt());
        doNothing().when(userRepository).delete(any(User.class));
        Assertions.assertTrue(userService.deleteUserById(1));
    }
}
