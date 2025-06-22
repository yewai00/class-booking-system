package com.yewai.classBookingSystem.scheduler;

import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;
import com.yewai.classBookingSystem.features.notification.domain.service.NotificationService;
import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@DisallowConcurrentExecution
@AllArgsConstructor
public class PackageExpiryJob extends QuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(PackageExpiryJob.class);

    private final UserPackageRepository userPackageRepository;

    private NotificationService notificationService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("PackageExpiryJob started");

        List<UserPackage> expiredPackages = userPackageRepository
                .findByStatusAndExpiryDateBefore(UserPackageStatus.ACTIVE, LocalDateTime.now());

        for (UserPackage userPackage : expiredPackages) {
            try {
                userPackage.setStatus(UserPackageStatus.EXPIRED);
                userPackageRepository.save(userPackage);

                String userEmail = userPackage.getUser().getEmail();
                notificationService.sendNotification(new NotificationRequest(userEmail, "Your Package Has Expired",
                    "Dear " + userPackage.getUser().getName() + ",\n\nYour " + userPackage.getPackageType().getName() + " package has expired. Please purchase a new one to continue booking classes."));

                log.info("Marked UserPackage ID {} as EXPIRED for User ID {}", userPackage.getId(), userPackage.getUser().getId());
            } catch (Exception e) {
                log.error("Error processing package expiry for UserPackage ID {}: {}", userPackage.getId(), e.getMessage(), e);
            }
        }

        List<UserPackage> depletedPackages = userPackageRepository
                .findByStatusAndCurrentCreditsLessThanEqual(UserPackageStatus.ACTIVE, 0);

        for (var userPackage : depletedPackages) {
            try {
                userPackage.setStatus(UserPackageStatus.DEPLETED);
                userPackageRepository.save(userPackage);
                log.info("Marked UserPackage ID {} as DEPLETED for User ID {}", userPackage.getId(), userPackage.getUser().getId());
            } catch (Exception e) {
                log.error("Error processing package depletion for UserPackage ID {}: {}", userPackage.getId(), e.getMessage(), e);
            }
        }

        log.info("PackageExpiryJob finished.");
    }
}
