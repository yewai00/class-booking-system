package com.yewai.classBookingSystem.features.booking.api.response;

import com.yewai.classBookingSystem.features.booking.domain.entity.WaitList;
import com.yewai.classBookingSystem.features.booking.domain.enums.WaitListStatus;

import java.time.LocalDateTime;

public record WaitListResponse(
        Long waitListId,
        Long classId,
        String className,
        LocalDateTime classStartTime,
        Integer requiredCredits,
        String packageName,
        WaitListStatus status,
        LocalDateTime waitlistTime,
        String countryCode
) {
    public static WaitListResponse from(
            WaitList waitList
    ) {
        return new WaitListResponse(
                waitList.getId(),
                waitList.getClassSchedule().getId(),
                waitList.getClassSchedule().getClassName(),
                waitList.getClassSchedule().getStartTime(),
                waitList.getRequiredCreditsAtWaitlist(),
                waitList.getUserPackage().getPackageType().getName(),
                waitList.getStatus(),
                waitList.getWaitlistTime(),
                waitList.getClassSchedule().getCountryCode()
        );
    }
}
