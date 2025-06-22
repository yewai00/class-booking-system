package com.yewai.classBookingSystem.scheduler;

import com.yewai.classBookingSystem.features.booking.domain.entity.WaitList;
import com.yewai.classBookingSystem.features.booking.domain.enums.WaitListStatus;
import com.yewai.classBookingSystem.features.booking.domain.repo.ClassRepository;
import com.yewai.classBookingSystem.features.booking.domain.repo.WaitListRepository;
import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;
import com.yewai.classBookingSystem.features.notification.domain.service.NotificationService;
import com.yewai.classBookingSystem.features.pkg.domain.enums.UserPackageStatus;
import com.yewai.classBookingSystem.features.pkg.domain.repo.UserPackageRepository;
import lombok.AllArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@DisallowConcurrentExecution
@AllArgsConstructor
public class WaitlistRefundJob extends QuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(WaitlistRefundJob.class);

    private final WaitListRepository waitlistRepository;
    private final UserPackageRepository userPackageRepository;
    private final ClassRepository classScheduleRepository;
    private final NotificationService notificationService;
    private static final long REFUND_PROCESSING_GRACE_PERIOD_HOURS = 1;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("WaitlistRefundJob started");
        var thresholdTime = LocalDateTime.now().minusHours(REFUND_PROCESSING_GRACE_PERIOD_HOURS);

        List<WaitList> waitingListEntries = waitlistRepository
                .findByStatusAndClassScheduleEndTimeBefore(WaitListStatus.WAITING, thresholdTime);


        for (var waitlistEntry : waitingListEntries) {
            try {
                if (waitlistEntry.getStatus() != WaitListStatus.WAITING) {
                    continue;
                }

                var waitList = waitlistRepository.findByUserPackageId(waitlistEntry.getUserPackage().getId())
                        .orElseThrow(() -> new RuntimeException("User package not found for waitlist entry: " + waitlistEntry.getId()));

                var userPackage = waitList.getUserPackage();
                var classSchedule = classScheduleRepository.findById(waitlistEntry.getClassSchedule().getId())
                        .orElseThrow(() -> new RuntimeException("Class schedule not found for waitlist entry: " + waitlistEntry.getId()));

                int creditsToRefund = waitlistEntry.getRequiredCreditsAtWaitlist();
                userPackage.setCurrentCredits(userPackage.getCurrentCredits() + creditsToRefund);

                if (userPackage.getStatus() == UserPackageStatus.DEPLETED && userPackage.getCurrentCredits() > 0) {
                    userPackage.setStatus(UserPackageStatus.ACTIVE);
                }
                userPackageRepository.save(userPackage);

                waitlistEntry.setStatus(WaitListStatus.CREDIT_REFUNDED);
                waitlistRepository.save(waitlistEntry);

                String userEmail = waitlistEntry.getUser().getEmail();
                notificationService.sendNotification(new NotificationRequest(userEmail,"WaitList Refund", classSchedule.getClassName()));

                log.info("Refunded {} credits to user {} for class {} (Waitlist ID: {})",
                        creditsToRefund, waitlistEntry.getUser().getId(), classSchedule.getId(), waitlistEntry.getId());

            } catch (Exception e) {
                log.error("Error processing waitlist refund for entry {}: {}", waitlistEntry.getId(), e.getMessage(), e);
            }
        }
        log.info("WaitlistRefundJob finished.");
    }
}
