package dev.carrynong.goutbackend.auth;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.auth.service.CustomUserDetailService;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyLoginRepository;
import dev.carrynong.goutbackend.user.model.UserRole;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {
    @InjectMocks
    private CustomUserDetailService customUserDetailService;
    @Mock
    private UserLoginRepository userLoginRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private TourCompanyLoginRepository tourCompanyLoginRepository;

    @BeforeEach
    void setup() {
        // Any setup needed before each test
    }

    @Test
    void testLoadUserByUsernameWithEmail() {
        // Given
        String email = "user@example.com";
        UserLogin userLogin = new UserLogin(
                null,
                AggregateReference.to(1),
                email,
                "password"
        );
        UserRole userRole = new UserRole(
                null,
                AggregateReference.to(1),
                AggregateReference.to(RoleEnum.CONSUMER.getId())
        );

        when(userLoginRepository.findByEmail(email)).thenReturn(Optional.of(userLogin));
        when(userRoleRepository.findByUserId(AggregateReference.to(1))).thenReturn(Optional.of(userRole));

        // When
        AuthenticateUser userDetails = (AuthenticateUser) customUserDetailService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(1, userDetails.userId());
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleEnum.CONSUMER.name())));
    }

    @Test
    void testLoadUserByUsernameWithEmailNotFound() {
        // Given
        String email = "user@example.com";
        when(userLoginRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername(email);
        });
        assertEquals(String.format("Credential for %s not found", email), thrown.getMessage());
    }

    @Test
    void testLoadUserByUsernameWithTourCompanyUsername() {
        // Given
        String username = "companyUsername";
        TourCompanyLogin tourCompanyLogin = new TourCompanyLogin(
                1,
                AggregateReference.to(1),
                username,
                "password"
        );

        when(tourCompanyLoginRepository.findOneByUsername(username)).thenReturn(Optional.of(tourCompanyLogin));

        // When
        AuthenticateUser userDetails = (AuthenticateUser) customUserDetailService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(1, userDetails.userId());
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleEnum.COMPANY.name())));
    }

    @Test
    void testLoadUserByUsernameWithTourCompanyUsernameNotFound() {
        // Given
        String username = "companyUsername";
        when(tourCompanyLoginRepository.findOneByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername(username);
        });
        assertEquals(String.format("Credential for %s not found", username), thrown.getMessage());
    }
}