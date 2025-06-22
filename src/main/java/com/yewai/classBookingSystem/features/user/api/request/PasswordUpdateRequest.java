package com.yewai.classBookingSystem.features.user.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @Size(min = 8, max = 255)
        @NotBlank(message = "currentPassword is required.")
        String currentPassword,

        @Size(min = 8, max = 255)
        @NotBlank(message = "newPassword is required.")
        String newPassword
) {
}
