package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.LoginResponseDTO;
import com.websocket.WebsocketProject.dto.UserResponseDTO;
import com.websocket.WebsocketProject.entity.LoginRequest;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody User user){
        UserResponseDTO savedUser = userService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequest request) {

        LoginResponseDTO response =
                userService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Principal principal) {
        if (principal != null) {
            userService.updateOnlineStatus(principal.getName(), false);
        }
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId) {
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
