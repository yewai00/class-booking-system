package com.yewai.classBookingSystem.features.booking.api.response;

import com.yewai.classBookingSystem.features.booking.domain.entity.Clazz;

import java.time.LocalDateTime;

public record ClassResponse(
        Long id,
        String className,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer requiredCredits,
        Integer capacity,
        Integer bookedSlots,
        Integer availableSlots,
        String countryCode,
        String instructorName,
        String location
) {
    public static ClassResponse from(
            Clazz clazz,
            Integer availableSlots,
            Integer bookedSlots
    ) {
        return new ClassResponse(
                clazz.getId(),
                clazz.getClassName(),
                clazz.getDescription(),
                clazz.getStartTime(),
                clazz.getEndTime(),
                clazz.getRequiredCredits(),
                clazz.getCapacity(),
                bookedSlots,
                availableSlots,
                clazz.getCountryCode(),
                clazz.getInstructorName(),
                clazz.getLocation()
        );
    }
}
