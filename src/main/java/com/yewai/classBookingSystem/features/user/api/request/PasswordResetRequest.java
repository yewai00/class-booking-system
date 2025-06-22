package com.yewai.classBookingSystem.features.user.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @Size(max = 255)
        @NotBlank(message = "Token is required.")
        String token,
        @Size(min = 8, max = 255)
        @NotBlank(message = "New Password is required.")
        String newPassword
) {
}
