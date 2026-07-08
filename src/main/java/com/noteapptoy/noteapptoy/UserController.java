package com.noteapptoy.noteapptoy;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        userService.deleteUser(authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateUser(Authentication authentication, @Valid @RequestBody UserRequest request) {
        UserDto updatedUser = userService.updateUser(authentication.getName(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updatePartUser(Authentication authentication, @Valid @RequestBody UserPartRequest request) {
        UserDto updatedUser = userService.updatePartUser(authentication, request);
        return ResponseEntity.ok(updatedUser);
    }
}
