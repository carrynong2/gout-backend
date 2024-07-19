package dev.carrynong.goutbackend.auth.model;

import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user_login")
public record UserLogin(
        @Id Integer id,
        AggregateReference<User, Integer> userId,
        String email,
        String password
) {
}
