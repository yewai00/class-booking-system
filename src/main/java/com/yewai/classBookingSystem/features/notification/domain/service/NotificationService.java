package com.yewai.classBookingSystem.features.notification.domain.service;

import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest notificationRequest);
}
