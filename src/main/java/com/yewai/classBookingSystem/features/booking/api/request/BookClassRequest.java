package com.yewai.classBookingSystem.features.booking.api.request;

import jakarta.validation.constraints.NotNull;

public record BookClassRequest(
        @NotNull(message = "classId is required!")
        Long classId,
        @NotNull(message = "userPackageId is required.")
        Long userPackageId
) {
}
