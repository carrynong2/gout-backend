package dev.carrynong.goutbackend.user;

import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.model.Role;
import dev.carrynong.goutbackend.user.model.UserRole;
import dev.carrynong.goutbackend.user.repository.RoleRepository;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import dev.carrynong.goutbackend.user.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @InjectMocks
    private RoleService roleService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @Test
    void shouldReturnRoles() {
        var mockRoles = List.of(
                new Role(RoleEnum.COMPANY.getId(), RoleEnum.CONSUMER.name()),
                new Role(RoleEnum.ADMIN.getId(), RoleEnum.ADMIN.name()),
                new Role(RoleEnum.COMPANY.getId(), RoleEnum.COMPANY.name())
        );
        when(roleRepository.findAll())
                .thenReturn(mockRoles);

        var actual = roleService.getAllRole();
        List<Role> result = new ArrayList<>();
        actual.iterator().forEachRemaining(result::add);

        assertEquals(3, result.size());
    }

    @Test
    void shouldBindNewUserRole() {
        // Given
        int userId = 1;
        RoleEnum roleEnum = RoleEnum.ADMIN;
        var userRole = new UserRole(null, AggregateReference.to(userId), AggregateReference.to(roleEnum.getId()));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);

        // When
        var actual = roleService.bindingNewUser(userId, roleEnum);

        // Then
        assertNotNull(actual);
        assertEquals(userId, actual.userId().getId());
        assertEquals(roleEnum.getId(), actual.roleId().getId());
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingRoleForNonExistentUser() {
        // Given
        int userId = 1;
        when(userRoleRepository.findByUserId(AggregateReference.to(userId)))
                .thenReturn(Optional.empty());

        // When & Then
        var exception = assertThrows(EntityNotFoundException.class, () -> roleService.deleteRoleByUserId(userId));
        assertEquals(String.format("Role for username: %d not found", userId), exception.getMessage());
        verify(userRoleRepository, times(1)).findByUserId(AggregateReference.to(userId));
        verify(userRoleRepository, never()).delete(any(UserRole.class));
    }

    @Test
    void shouldDeleteUserRoleSuccessfully() {
        // Given
        int userId = 1;
        var userRole = new UserRole(1, AggregateReference.to(userId), AggregateReference.to(RoleEnum.ADMIN.getId()));
        when(userRoleRepository.findByUserId(AggregateReference.to(userId))).thenReturn(Optional.of(userRole));

        // When
        roleService.deleteRoleByUserId(userId);

        // Then
        verify(userRoleRepository, times(1)).findByUserId(AggregateReference.to(userId));
        verify(userRoleRepository, times(1)).delete(userRole);
    }
}
