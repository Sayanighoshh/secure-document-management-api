package com.sayani.secure_auth_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String status;      // "SUCCESS", "OTP_REQUIRED", "REGISTERED"
    private String token;
    private String tokenType;   // "Bearer"
    private String name;
    private String email;
    private String role;
    private String message;
    private Boolean twoFactorRequired;
    private String deviceId;
}
