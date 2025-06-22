package com.yewai.classBookingSystem.features.pkg.api.response;

import com.yewai.classBookingSystem.features.pkg.domain.entity.Package;

import java.math.BigDecimal;

public record PackageResponse(
        Long id,
        String name,
        String description,
        Integer credits,
        BigDecimal price,
        Integer validityDays,
        String countryCode
) {
    public static PackageResponse from(Package pkg) {
        return new PackageResponse(
                pkg.getId(),
                pkg.getName(),
                pkg.getDescription(),
                pkg.getCredits(),
                pkg.getPrice(),
                pkg.getValidityDays(),
                pkg.getCountryCode()
        );
    }
}
