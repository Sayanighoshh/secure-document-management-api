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
public class RegisterRequest {

    @NotBlank(message = "Full name cannot be blank")
    private String name;

    private String username;
    private String email;

    // Optional: if not provided or blank, password will be auto-generated in format <FirstName>@<number>
    private String password;

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }

    public String getName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return getEmail();
    }
}
