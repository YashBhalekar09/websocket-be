package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.dto.LoginResponseDTO;
import com.websocket.WebsocketProject.dto.UserResponseDTO;
import com.websocket.WebsocketProject.entity.RefreshToken;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponseDTO register(User user, MultipartFile profilePicture){
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setOnline(false);

        if(profilePicture !=null && !profilePicture.isEmpty()){
            String fileName = UUID.randomUUID() + "_" + profilePicture.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/profile-pictures");
            try {
                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(profilePicture.getInputStream(), filePath,
                        StandardCopyOption.REPLACE_EXISTING);

                user.setProfilePicture("/uploads/profile-pictures/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile picture", e);
            }
        }

        User userData= userRepository.save(user);

        return  new UserResponseDTO(
                userData.getId(),
                userData.getUsername(),
                userData.getEmail(),
                userData.isOnline(),
                userData.getProfilePicture()
        );
    }

    public LoginResponseDTO login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            throw new RuntimeException("Invalid email or password");
        }
        String accessToken = jwtService.generateToken(user.getId(), user.getUsername(),user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), null, null);

        return new LoginResponseDTO(accessToken,refreshToken.getToken(), user.getUsername(), user.getId());
    }

    public UserResponseDTO getUserById(Long userId) {

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDTO(user.getId(), user.getUsername(),user.getEmail(),user.isOnline(), user.getProfilePicture());
    }

    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                                user.getId(), user.getUsername(),
                                user.getEmail(), user.isOnline(), user.getProfilePicture()
                        )
                ).toList();
    }

    @Transactional
    public void updateOnlineStatus(String username, boolean online) {

        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException(
                                        "User not found: " + username));
        user.setOnline(online);
        userRepository.save(user);
    }
}
