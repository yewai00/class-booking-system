package com.yewai.classBookingSystem.features.notification.domain.service;


import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Async("asyncExecutor")
    @Override
    public void sendNotification(NotificationRequest notificationRequest) {
        try {
            log.info("Email sent to: {}", notificationRequest.to());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", notificationRequest.to(), e);
        }
    }
}
