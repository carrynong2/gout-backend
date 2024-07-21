package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyLoginRepository;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import org.apache.commons.validator.routines.EmailValidator;
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
    private final TourCompanyLoginRepository tourCompanyLoginRepository;

    public CustomUserDetailService(UserLoginRepository userLoginRepository, UserRoleRepository userRoleRepository, TourCompanyLoginRepository tourCompanyLoginRepository) {
        this.userLoginRepository = userLoginRepository;
        this.userRoleRepository = userRoleRepository;
        this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (isEmail(username)) {
            return userFlow(username);
        }
        return tourCompanyFlow(username);

    }

    private boolean isEmail(String username) {
        return EmailValidator.getInstance().isValid(username);
    }

    private AuthenticateUser userFlow(String username) {
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

    private AuthenticateUser tourCompanyFlow(String username) {
        var tourCompanyLogin = tourCompanyLoginRepository.findOneByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Credential for %s not found", username)));
        return new AuthenticateUser(tourCompanyLogin.id(), tourCompanyLogin.username(), tourCompanyLogin.password(), RoleEnum.COMPANY);
    }

}
