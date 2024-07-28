package dev.carrynong.goutbackend.user.service;

import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> getUserByFirstName(String keyword, Pageable pageable);
    UserInfoDTO getUserDTOById(Integer id);
    User getUserById(Integer id);
    UserInfoDTO createUser(UserCreationDTO body);
    UserInfoDTO updateUser(Integer id,UserUpdateDTO body);
    boolean deleteUserById(Integer id);
}
