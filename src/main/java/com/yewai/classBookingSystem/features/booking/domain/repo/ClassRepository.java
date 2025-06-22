package com.yewai.classBookingSystem.features.booking.domain.repo;

import com.yewai.classBookingSystem.features.booking.domain.entity.Clazz;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Clazz, Long> {

    List<Clazz> findByCountryCodeAndStartTimeBetweenOrderByStartTimeAsc(String countryCode, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("""
            SELECT cs FROM Clazz cs JOIN Booking b ON b.classSchedule.id = cs.id
            WHERE b.user.id = :userId
            AND b.status IN ('CONFIRMED', 'CHECKED_IN')
            AND (:newClassStartTime < cs.endTime AND :newClassEndTime > cs.startTime)
            """)
    List<Clazz> findOverlappingBookingsForUser(
            @Param("userId") Long userId,
            @Param("newClassStartTime") LocalDateTime newClassStartTime,
            @Param("newClassEndTime") LocalDateTime newClassEndTime
    );

}
