package dev.carrynong.goutbackend.user.service;

import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;

public interface UserService {
    UserInfoDTO getUserDTOById(Integer id);
    User getUserById(Integer id);
    UserInfoDTO createUser(UserCreationDTO body);
    UserInfoDTO updateUser(Integer id,UserUpdateDTO body);
    boolean deleteUserById(Integer id);
}
