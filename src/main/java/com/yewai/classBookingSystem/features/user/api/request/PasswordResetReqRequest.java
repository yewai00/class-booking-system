package com.yewai.classBookingSystem.features.user.api.request;

import jakarta.validation.constraints.Email;

public record PasswordResetReqRequest(
        @Email
        String email
) {
}
