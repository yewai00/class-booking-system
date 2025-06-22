package com.yewai.classBookingSystem.features.booking.domain.repo;

import com.yewai.classBookingSystem.features.booking.domain.entity.Booking;
import com.yewai.classBookingSystem.features.booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);

    boolean existsByUserIdAndClassScheduleIdAndStatus(Long userId, Long classScheduleId, BookingStatus status);

}
