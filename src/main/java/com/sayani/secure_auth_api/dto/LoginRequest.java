package com.sayani.secure_auth_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    // Can be sent as "username" or "email"
    private String username;
    private String email;

    // Credentials password (optional if logging in directly with token)
    private String password;

    // Optional: JWT token if logging in using token received from verify-login
    private String token;

    // Optional device identifier (e.g. "device-A", "device-1", or mobile ID)
    private String deviceId;

    public String getUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email;
    }

    public String getDeviceId() {
        if (deviceId != null && !deviceId.isBlank()) {
            return deviceId;
        }
        return "default-device";
    }
}
