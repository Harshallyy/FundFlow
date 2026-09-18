package com.fundflow.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // "DONOR" or "ORGANIZER" only - admins are never self-registered.
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "DONOR|ORGANIZER", message = "Role must be DONOR or ORGANIZER")
    private String role;
}
