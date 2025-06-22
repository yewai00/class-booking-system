package com.yewai.classBookingSystem.features.booking.api.response;

public record CancellationResponse(
        String message,
        Boolean creditRefund,
        Boolean waitlistPromoted
) {
}
