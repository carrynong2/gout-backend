package dev.carrynong.goutbackend.user.controller;

import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class UserSelfManageController {
    private final UserService userService;

    public UserSelfManageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserInfoDTO> getUserById(Authentication authentication) {
        var id = getMyId(authentication);
        return ResponseEntity.ok(userService.getUserDTOById(id));
    }

    @PatchMapping
    public ResponseEntity<UserInfoDTO> updateUser(Authentication authentication,@RequestBody @Validated UserUpdateDTO body) {
        var id = getMyId(authentication);
        return ResponseEntity.ok(userService.updateUser(id, body));
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteUser(Authentication authentication) {
        var id = getMyId(authentication);
        userService.deleteUserById(id);
        return ResponseEntity.ok(true);
    }

    private int getMyId(Authentication authentication) {
        var jwt = (Jwt) authentication.getPrincipal();
        return Integer.parseInt(jwt.getClaimAsString("sub"));
    }

}
