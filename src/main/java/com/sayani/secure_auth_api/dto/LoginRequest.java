package com.sayani.secure_auth_api.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Password cannot be blank")
    private String password;

    // Optional device identifier (e.g. "device-1", "my-laptop", or mobile ID)
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
