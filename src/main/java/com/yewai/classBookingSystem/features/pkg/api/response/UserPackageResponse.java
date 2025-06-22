package com.yewai.classBookingSystem.features.pkg.api.response;

import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
import com.yewai.classBookingSystem.features.pkg.domain.enums.UserPackageStatus;

import java.time.LocalDateTime;

public record UserPackageResponse(
        Long userPackageId,
        String packageName,
        Integer currentCredits,
        Integer totalCredits,
        LocalDateTime purchaseDate,
        LocalDateTime expiryDate,
        UserPackageStatus status,
        String countryCode
) {
    public static UserPackageResponse from(UserPackage userPackage) {
        return new UserPackageResponse(
                userPackage.getId(),
                userPackage.getPackageType().getName(),
                userPackage.getCurrentCredits(),
                userPackage.getTotalCredits(),
                userPackage.getPurchaseDate(),
                userPackage.getExpiryDate(),
                userPackage.getStatus(),
                userPackage.getCountryCode()
        );
    }
}

