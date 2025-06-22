package com.yewai.classBookingSystem.features.user.api.response;

import com.yewai.classBookingSystem.features.user.domain.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String createdAt,
        String updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt().toString(),
                user.getUpdatedAt().toString()
        );
    }
}
