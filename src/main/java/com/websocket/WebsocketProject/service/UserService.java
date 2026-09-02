package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.dto.LoginResponseDTO;
import com.websocket.WebsocketProject.dto.OtpLoginResponseDTO;
import com.websocket.WebsocketProject.dto.UserResponseDTO;
import com.websocket.WebsocketProject.entity.OtpVerification;
import com.websocket.WebsocketProject.entity.RefreshToken;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.repository.OtpVerificationRepository;
import com.websocket.WebsocketProject.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final OtpVerificationRepository otpVerificationRepository;
    private final JavaMailSender javaMailSender;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, OtpVerificationRepository otpVerificationRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.otpVerificationRepository = otpVerificationRepository;
        this.javaMailSender = javaMailSender;
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

//    public LoginResponseDTO login(String email, String password) {
//        User user = userRepository.findByEmail(email).orElseThrow(() ->
//                        new RuntimeException("Invalid email or password"));
//
//        boolean matches = passwordEncoder.matches(password, user.getPassword());
//        if (!matches) {
//            throw new RuntimeException("Invalid email or password");
//        }
//        String accessToken = jwtService.generateToken(user.getId(), user.getUsername(),user.getEmail());
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), null, null);
//
//        return new LoginResponseDTO(accessToken,refreshToken.getToken(), user.getUsername(), user.getId());
//    }

    public OtpLoginResponseDTO login(String email, String password) {

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate 6 digit OTP
        String otp = generateOtp();

        otpVerificationRepository
                .findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(user.getId())
                .ifPresent(oldOtp -> {
                    oldOtp.setVerified(true);
                    otpVerificationRepository.save(oldOtp);
                });

        // Create OTP record
        OtpVerification otpVerification = new OtpVerification();

        otpVerification.setUser(user);
        otpVerification.setOtp(otp);
        otpVerification.setCreatedAt(LocalDateTime.now());
        otpVerification.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );
        otpVerification.setAttempts(0);
        otpVerification.setVerified(false);

        otpVerificationRepository.save(otpVerification);

        // Send OTP
        sendOtpEmail(user.getEmail(), otp);

        return new OtpLoginResponseDTO("OTP sent successfully to your email");
    }

    public LoginResponseDTO verifyOtp(String email, String otp, HttpServletRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        OtpVerification verification =
                otpVerificationRepository
                        .findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(user.getId())
                        .orElseThrow(() -> new RuntimeException("OTP not found"));

        // Check if OTP has expired
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // Check maximum attempts before validating OTP
        if (verification.getAttempts() >= 5) {
            throw new RuntimeException("Too many invalid OTP attempts");
        }

        // Check OTP
        if (!verification.getOtp().equals(otp)) {

            int attempts = verification.getAttempts() + 1;
            verification.setAttempts(attempts);
            otpVerificationRepository.saveAndFlush(verification);

            if (attempts >= 5) {
                throw new RuntimeException("Too many invalid OTP attempts");
            }
            throw new RuntimeException("Invalid OTP");
        }

        // OTP is correct
        verification.setVerified(true);
        otpVerificationRepository.saveAndFlush(verification);

        // Generate access token
        String accessToken = jwtService.generateToken(
                user.getId(), user.getUsername(), user.getEmail());

        // Generate refresh token
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());

        return new LoginResponseDTO(
                accessToken, refreshToken.getToken(), user.getUsername(), user.getId());
    }

    private void sendOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Your Login OTP");

        message.setText("Your OTP for login is: " + otp +
                "\n\nThis OTP is valid for 5 minutes." +
                "\n\nPlease do not share this OTP with anyone.");

        javaMailSender.send(message);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
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
