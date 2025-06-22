package com.yewai.classBookingSystem.features.notification.api.request;

public record NotificationRequest(
        String to,
        String subject,
        String message
) {
}
