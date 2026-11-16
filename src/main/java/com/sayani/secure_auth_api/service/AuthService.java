package com.sayani.secure_auth_api.service;

import com.sayani.secure_auth_api.dto.AuthResponse;
import com.sayani.secure_auth_api.dto.LoginRequest;
import com.sayani.secure_auth_api.dto.RegisterRequest;
import com.sayani.secure_auth_api.dto.VerifyOtpRequest;
import com.sayani.secure_auth_api.entity.Role;
import com.sayani.secure_auth_api.entity.User;
import com.sayani.secure_auth_api.repository.UserRepository;
import com.sayani.secure_auth_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User is already registered: " + email);
        }

        var user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .twoFactorEnabled(true)
                .build();

        userRepository.save(user);

        // Send registration email with username and password confirmation
        emailService.sendRegistrationEmail(user.getEmail(), user.getName(), request.getPassword());

        return AuthResponse.builder()
                .status("REGISTERED")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("User registered successfully. Credentials sent to registered email.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String loginIdentifier = request.getUsername();
        if (loginIdentifier == null || loginIdentifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        // 1. Authenticate user credentials (username/email and password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginIdentifier,
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var user = userRepository.findByEmail(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        String requestedDevice = request.getDeviceId();
        boolean isRecognizedDevice = user.getLastKnownDeviceId() != null && user.getLastKnownDeviceId().equals(requestedDevice);

        // 2. Unrecognized device or first login -> Trigger Two-Factor Authentication (OTP)
        if (!isRecognizedDevice && user.isTwoFactorEnabled()) {
            String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
            user.setCurrentOtp(otp);
            user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);

            // Send OTP alert email to the registered user
            emailService.sendOtpEmail(user.getEmail(), user.getName(), otp, requestedDevice);

            return AuthResponse.builder()
                    .status("OTP_REQUIRED")
                    .email(user.getEmail())
                    .name(user.getName())
                    .twoFactorRequired(true)
                    .deviceId(requestedDevice)
                    .message("Unrecognized device detected. A 6-digit OTP has been sent to your registered email.")
                    .build();
        }

        // 3. Recognized / trusted device -> Directly issue JWT
        var jwtToken = jwtService.generateTokenFromCredentials(
                user.getUsername(),
                request.getPassword(),
                Map.of("role", user.getRole().name())
        );

        return AuthResponse.builder()
                .status("SUCCESS")
                .token(jwtToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .deviceId(requestedDevice)
                .twoFactorRequired(false)
                .message("Authentication successful (Recognized Device)")
                .build();
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String loginIdentifier = request.getUsername();
        if (loginIdentifier == null || loginIdentifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        var user = userRepository.findByEmail(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + loginIdentifier));

        // Validate OTP existence
        if (user.getCurrentOtp() == null || !user.getCurrentOtp().equals(request.getOtp().trim())) {
            throw new IllegalArgumentException("Invalid OTP code provided.");
        }

        // Validate OTP expiry
        if (user.getOtpExpiryTime() == null || user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP code has expired. Please initiate login again.");
        }

        // Mark device as recognized and clear the used OTP
        user.setLastKnownDeviceId(request.getDeviceId());
        user.setCurrentOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        // Issue JWT token upon successful 2FA verification
        var jwtToken = jwtService.generateToken(
                Map.of("role", user.getRole().name()),
                user.getUsername()
        );

        return AuthResponse.builder()
                .status("SUCCESS")
                .token(jwtToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .deviceId(request.getDeviceId())
                .twoFactorRequired(false)
                .message("Two-Factor Authentication verified successfully. Device registered.")
                .build();
    }
}
