package com.yewai.classBookingSystem.features.pkg.domain.repo;

import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
import com.yewai.classBookingSystem.features.pkg.domain.enums.UserPackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUserId(Long userId);

    Optional<UserPackage> findByIdAndUserIdAndStatus(Long id, Long userId, UserPackageStatus status);

    List<UserPackage> findByUserIdAndStatusOrderByExpiryDateAsc(Long userId, UserPackageStatus status);

    @Query("""
            SELECT up FROM UserPackage up
            JOIN FETCH up.user u
            JOIN FETCH up.packageType pt
            WHERE up.status = :status AND up.expiryDate < :currentDateTime
            """)
    List<UserPackage> findByStatusAndExpiryDateBefore(
            @Param("status") UserPackageStatus status,
            @Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("""
            SELECT up FROM UserPackage up
            JOIN FETCH up.user u
            JOIN FETCH up.packageType pt
            WHERE up.status = :status AND up.currentCredits <= :credits
            """)
    List<UserPackage> findByStatusAndCurrentCreditsLessThanEqual(
            @Param("status") UserPackageStatus status,
            @Param("credits") int credits);
}
