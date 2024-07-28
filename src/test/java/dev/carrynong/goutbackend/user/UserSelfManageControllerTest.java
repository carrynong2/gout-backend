package dev.carrynong.goutbackend.user;

import dev.carrynong.goutbackend.user.controller.UserSelfManageController;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSelfManageControllerTest {
    @InjectMocks
    private UserSelfManageController userSelfManageController;
    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;

    @BeforeEach
    void setup() {
        // Setup any common mock behavior here
        when(authentication.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void testGetUserById() {
        // Given
        int userId = 1;
        UserInfoDTO userInfo = new UserInfoDTO(userId, "user@example.com", "User Name", "053-455-9999");
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));
        when(userService.getUserDTOById(userId)).thenReturn(userInfo);

        // When
        ResponseEntity<UserInfoDTO> response = userSelfManageController.getUserById(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userInfo, response.getBody());
        verify(userService, times(1)).getUserDTOById(userId);
    }

    @Test
    void testUpdateUser() {
        // Given
        int userId = 1;
        UserUpdateDTO updateDTO = new UserUpdateDTO("newUser@example.com", "New User Name");
        UserInfoDTO updatedUserInfo = new UserInfoDTO(userId, "newUser@example.com", "New User Name", "053-455-9999");
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));
        when(userService.updateUser(userId, updateDTO)).thenReturn(updatedUserInfo);

        // When
        ResponseEntity<UserInfoDTO> response = userSelfManageController.updateUser(authentication, updateDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUserInfo, response.getBody());
        verify(userService, times(1)).updateUser(userId, updateDTO);
    }

    @Test
    void testDeleteUser() {
        // Given
        int userId = 1;
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));

        // When
        ResponseEntity<Boolean> response = userSelfManageController.deleteUser(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(userService, times(1)).deleteUserById(userId);
    }
}