package dev.carrynong.goutbackend.user.controller;

import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserDTOById(id));
    }

    @PostMapping
    public ResponseEntity<UserInfoDTO> createUser(@RequestBody @Validated UserCreationDTO body) {
        var newUser = userService.createUser(body);
        var location = String.format("http://localhost/api/v1/users/%d", newUser.id());
        return ResponseEntity.created(URI.create(location)).body(newUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserInfoDTO> updateUser(@PathVariable Integer id,
                                                  @RequestBody @Validated UserUpdateDTO body) {
        return ResponseEntity.ok(userService.updateUser(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Integer id) {
        userService.deleteUserById(id);
        logger.info("UserId: {} has been deleted", id);
        return ResponseEntity.ok(true);
    }

}
