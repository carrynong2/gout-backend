package dev.carrynong.goutbackend.user.controller;

import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping("")
    public Page<User> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = true) int page,
            @RequestParam(required = true) int size,
            @RequestParam(required = true) String sortField,
            @RequestParam(required = true) String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return userService.getUserByFirstName(keyword,pageable);
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
