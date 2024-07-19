package dev.carrynong.goutbackend.user.repository;

import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.model.UserRole;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRoleRepository extends CrudRepository<UserRole, Integer> {
    Optional<UserRole> findByUserId(AggregateReference<User, Integer> userId);
}
