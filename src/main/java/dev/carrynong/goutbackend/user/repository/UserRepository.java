package dev.carrynong.goutbackend.user.repository;

import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
