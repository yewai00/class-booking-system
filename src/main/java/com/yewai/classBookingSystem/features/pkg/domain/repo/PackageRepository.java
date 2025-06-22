package com.yewai.classBookingSystem.features.pkg.domain.repo;

import com.yewai.classBookingSystem.features.pkg.domain.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByCountryCodeAndIsActiveTrue(String countryCode);
}
