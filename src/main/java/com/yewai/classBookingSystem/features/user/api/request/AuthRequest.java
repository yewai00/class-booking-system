package com.yewai.classBookingSystem.features.user.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @Email
        @NotEmpty(message = "Email is required field.")
        @Size(max = 255)
        String email,

        @NotEmpty(message = "Password is required field.")
        @Size(min = 8, max = 255)
        String password
) {
}
