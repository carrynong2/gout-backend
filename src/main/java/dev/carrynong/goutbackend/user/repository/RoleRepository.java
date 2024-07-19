package dev.carrynong.goutbackend.user.repository;

import dev.carrynong.goutbackend.user.model.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer> {
}
