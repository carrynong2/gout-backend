package dev.carrynong.goutbackend.auth.dto;

import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AuthenticateUser(
        Integer userId,
        String email,
        String password,
        RoleEnum role
) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (this.role) {
            case RoleEnum.ADMIN -> List.of(new SimpleGrantedAuthority(RoleEnum.ADMIN.name()));
            default -> List.of(new SimpleGrantedAuthority(RoleEnum.CONSUMER.name()));
        };
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
