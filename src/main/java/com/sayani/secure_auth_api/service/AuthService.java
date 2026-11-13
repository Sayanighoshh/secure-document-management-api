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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        var jwtToken = jwtService.generateToken(user);
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
