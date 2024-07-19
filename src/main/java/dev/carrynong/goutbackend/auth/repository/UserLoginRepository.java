package dev.carrynong.goutbackend.auth.repository;

import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserLoginRepository extends CrudRepository<UserLogin, Integer> {
    Optional<UserLogin> findByEmail(String email);
    Optional<UserLogin> findByUserId(AggregateReference<User, Integer> userId);
}
