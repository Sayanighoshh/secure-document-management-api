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
public class VerifyOtpRequest {

    // Can be sent as "username" or "email"
    private String username;
    private String email;

    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    // The device to register/trust upon verification
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
