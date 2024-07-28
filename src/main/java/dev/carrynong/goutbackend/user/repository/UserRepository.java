package dev.carrynong.goutbackend.user.repository;

import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<User, Integer> {
    Page<User> findByFirstNameContaining(String firstName,Pageable pageable);
}
