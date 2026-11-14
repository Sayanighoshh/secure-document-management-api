package com.sayani.secure_auth_api.service;

import com.sayani.secure_auth_api.dto.AuthResponse;
import com.sayani.secure_auth_api.dto.LoginRequest;
import com.sayani.secure_auth_api.dto.RegisterRequest;
import com.sayani.secure_auth_api.entity.Role;
import com.sayani.secure_auth_api.entity.User;
import com.sayani.secure_auth_api.repository.UserRepository;
import com.sayani.secure_auth_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
                .build();

        userRepository.save(user);

        // Generate token based on registered user credentials
        var jwtToken = jwtService.generateTokenFromCredentials(
                user.getUsername(),
                request.getPassword(),
                Map.of("role", user.getRole().name())
        );

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String loginIdentifier = request.getUsername();
        if (loginIdentifier == null || loginIdentifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        // Authenticate credentials (username/email and raw password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginIdentifier,
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var user = userRepository.findByEmail(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // Generate JWT based on verified login credentials (username and password)
        var jwtToken = jwtService.generateTokenFromCredentials(
                user.getUsername(),
                request.getPassword(),
                Map.of("role", user.getRole().name())
        );

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Authentication successful")
                .build();
    }
}
