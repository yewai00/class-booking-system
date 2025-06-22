package com.yewai.classBookingSystem.features.booking.api.response;

import com.yewai.classBookingSystem.features.booking.domain.entity.Booking;
import com.yewai.classBookingSystem.features.booking.domain.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        Long classId,
        String className,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer creditsDeducted,
        String packageName,
        BookingStatus status,
        LocalDateTime bookingTime,
        LocalDateTime checkInTime,
        String countryCode
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getClassSchedule().getId(),
                booking.getClassSchedule().getClassName(),
                booking.getClassSchedule().getStartTime(),
                booking.getClassSchedule().getEndTime(),
                booking.getCreditsDeducted(),
                booking.getUserPackage().getPackageType().getName(),
                booking.getStatus(),
                booking.getBookingTime(),
                booking.getCheckInTime(),
                booking.getClassSchedule().getCountryCode()
        );
    }
}
