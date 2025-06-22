package com.yewai.classBookingSystem.config;

import com.yewai.classBookingSystem.scheduler.PackageExpiryJob;
import com.yewai.classBookingSystem.scheduler.WaitlistRefundJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail waitlistRefundJobDetail() {
        return JobBuilder.newJob(WaitlistRefundJob.class)
                .withIdentity("waitlistRefundJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger waitlistRefundTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(waitlistRefundJobDetail())
                .withIdentity("waitlistRefundTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(30)
                        .repeatForever())
                .build();
    }

    // --- Package Expiry Job Configuration ---

    @Bean
    public JobDetail packageExpiryJobDetail() {
        return JobBuilder.newJob(PackageExpiryJob.class)
                .withIdentity("packageExpiryJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger packageExpiryTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(packageExpiryJobDetail())
                .withIdentity("packageExpiryTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(24) // Every 24 hours
                        .repeatForever())
                .startAt(DateBuilder.todayAt(0,0,0)) // Start at 00:00:00 today
                .build();
    }
}
