package com.yewai.classBookingSystem.features.pkg.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchasePackageRequest(
        @NotNull(message = "Package Id is required.")
        Long packageId,
        @NotBlank(message = "cardNumber is required.")
        String cardNumber,
        @NotBlank(message = "expiryDate is required.")
        String expiryDate,
        @NotBlank(message = "cvv is required.")
        String cvv
) {
}
