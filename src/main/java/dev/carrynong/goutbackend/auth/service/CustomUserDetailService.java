package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailService implements UserDetailsService {
    private final UserLoginRepository userLoginRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailService(UserLoginRepository userLoginRepository, UserRoleRepository userRoleRepository) {
        this.userLoginRepository = userLoginRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userLogin = userLoginRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Credential for %s not found", username)));
        var userId = userLogin.userId().getId();
        var userRole = userRoleRepository.findByUserId(AggregateReference.to(userId))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Role for username: %s not found", username)));
        var role = RoleEnum.CONSUMER;
        if (userRole.roleId().getId() == RoleEnum.ADMIN.getId()) {
            role = RoleEnum.ADMIN;
        }
        return new AuthenticateUser(userId, userLogin.email(), userLogin.password(), role);
    }

}
