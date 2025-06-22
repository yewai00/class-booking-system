package com.yewai.classBookingSystem.features.booking.domain.repo;

import com.yewai.classBookingSystem.features.booking.domain.entity.WaitList;
import com.yewai.classBookingSystem.features.booking.domain.enums.WaitListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitListRepository extends JpaRepository<WaitList, Long> {
    Optional<WaitList> findByUserPackageId(Long userPackageId);

    List<WaitList> findByUserId(Long userId);

    Optional<WaitList> findFirstByClassScheduleIdAndStatusOrderByWaitlistTimeAsc(Long classScheduleId, WaitListStatus status);

    @Query("""
            SELECT w FROM WaitList w JOIN FETCH w.classSchedule cs JOIN FETCH w.user u JOIN FETCH w.userPackage up
            WHERE w.status = :status AND cs.endTime < :endTimeThreshold
            """)
    List<WaitList> findByStatusAndClassScheduleEndTimeBefore(
            @Param("status") WaitListStatus status,
            @Param("endTimeThreshold") LocalDateTime endTimeThreshold);

    boolean existsByUserIdAndClassScheduleIdAndStatus(Long userId, Long classScheduleId, WaitListStatus status);
}
