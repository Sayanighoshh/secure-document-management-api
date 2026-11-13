package com.sayani.secure_auth_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Hello! You have accessed a secure endpoint protected by SSL/TLS and JWT!"
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
                "email", auth.getName(),
                "authorities", auth.getAuthorities(),
                "authenticated", auth.isAuthenticated()
        ));
    }
}
