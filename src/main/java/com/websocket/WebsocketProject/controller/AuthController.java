package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.LoginResponseDTO;
import com.websocket.WebsocketProject.dto.OtpLoginResponseDTO;
import com.websocket.WebsocketProject.dto.TokenResponseDTO;
import com.websocket.WebsocketProject.dto.UserResponseDTO;
import com.websocket.WebsocketProject.entity.LoginRequest;
import com.websocket.WebsocketProject.entity.RefreshToken;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.repository.UserRepository;
import com.websocket.WebsocketProject.service.JwtService;
import com.websocket.WebsocketProject.service.RefreshTokenService;
import com.websocket.WebsocketProject.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService, UserRepository userRepository, JwtService jwtService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserResponseDTO> register(

            @RequestPart("username")
            String username,

            @RequestPart("email")
            String email,

            @RequestPart("password")
            String password,

            @RequestPart(
                    value = "profilePicture",
                    required = false
            )
            MultipartFile profilePicture) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        UserResponseDTO response =
                userService.register(
                        user,
                        profilePicture
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<OtpLoginResponseDTO> login(
            @RequestBody LoginRequest request) {

        OtpLoginResponseDTO response =
                userService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Principal principal, @RequestParam(required = false) String refreshToken) {

        if (principal != null) {
            userService.updateOnlineStatus(principal.getName(), false);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            RefreshToken storedToken = refreshTokenService.validateRefreshToken(refreshToken);

            refreshTokenService.revokeToken(storedToken);
        }
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestParam String refreshToken) {

        //Validate old refresh token
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);
        // Find user
        User user = userRepository.findById(oldRefreshToken.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

        // Revoke old refresh token
        refreshTokenService.revokeToken(oldRefreshToken);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getEmail());

        // Generate new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), oldRefreshToken.getUserAgent(), oldRefreshToken.getCreatedByIpAddress());

        // Return both new tokens
        return ResponseEntity.ok(new TokenResponseDTO(newAccessToken, newRefreshToken.getToken()));
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

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponseDTO> verifyOtp(@RequestParam String email,
                                                      @RequestParam String otp, HttpServletRequest request) {

        LoginResponseDTO response = userService.verifyOtp(email, otp, request);

        return ResponseEntity.ok(response);
    }
}
