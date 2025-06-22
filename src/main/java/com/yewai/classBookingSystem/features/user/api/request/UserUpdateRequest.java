package com.yewai.classBookingSystem.features.user.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Email is required.")
        @Size(max = 255)
        @Email
        String email,

        @Size(min = 8,max = 255)
        String password
) {
}
