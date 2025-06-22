package com.yewai.classBookingSystem.features.booking.domain.entity;

import com.yewai.classBookingSystem.features.booking.domain.enums.WaitListStatus;
import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
import com.yewai.classBookingSystem.features.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "waitlists")
public class WaitList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Clazz classSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;

    @Column(name = "required_credits_at_waitlist", nullable = false)
    private Integer requiredCreditsAtWaitlist;

    @Column(name = "waitlist_time", nullable = false)
    private LocalDateTime waitlistTime;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private WaitListStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
