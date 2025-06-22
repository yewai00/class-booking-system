package com.yewai.classBookingSystem.features.booking.domain.service;

import com.yewai.classBookingSystem.exception.BaseException;
import com.yewai.classBookingSystem.features.booking.api.request.BookClassRequest;
import com.yewai.classBookingSystem.features.booking.api.response.BookingResponse;
import com.yewai.classBookingSystem.features.booking.api.response.CancellationResponse;
import com.yewai.classBookingSystem.features.booking.api.response.ClassResponse;
import com.yewai.classBookingSystem.features.booking.api.response.WaitListResponse;
import com.yewai.classBookingSystem.features.booking.domain.entity.Booking;
import com.yewai.classBookingSystem.features.booking.domain.entity.Clazz;
import com.yewai.classBookingSystem.features.booking.domain.entity.WaitList;
import com.yewai.classBookingSystem.features.booking.domain.enums.BookingStatus;
import com.yewai.classBookingSystem.features.booking.domain.enums.WaitListStatus;
import com.yewai.classBookingSystem.features.booking.domain.repo.BookingRepository;
import com.yewai.classBookingSystem.features.booking.domain.repo.ClassRepository;
import com.yewai.classBookingSystem.features.booking.domain.repo.WaitListRepository;
import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;
import com.yewai.classBookingSystem.features.notification.domain.service.NotificationService;
import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
import com.yewai.classBookingSystem.features.pkg.domain.enums.UserPackageStatus;
import com.yewai.classBookingSystem.features.pkg.domain.repo.UserPackageRepository;
import com.yewai.classBookingSystem.features.user.domain.entity.User;
import com.yewai.classBookingSystem.features.user.domain.repo.UserRepository;
import com.yewai.classBookingSystem.features.user.domain.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final ClassRepository classScheduleRepository;
    private final BookingRepository bookingRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WaitListRepository waitlistRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationService notificationService;

    public List<ClassResponse> getAvailableClassSchedules(String countryCode, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Clazz> classes = classScheduleRepository
                .findByCountryCodeAndStartTimeBetweenOrderByStartTimeAsc(countryCode, startOfDay, endOfDay);
        return classes.stream().map(c -> {
            String slotsKey = "class:schedule:" + c.getId() + ":slots_available";
            String availableSlotsStr = stringRedisTemplate.opsForValue().get(slotsKey);
            int availableSlots = (availableSlotsStr != null) ? Integer.parseInt(availableSlotsStr) : c.getCapacity();
            if (availableSlots < 0) availableSlots = 0; // Prevent negative display
            return ClassResponse.from(c,availableSlots,c.getCapacity() - availableSlots);
        }).toList();
    }

    @Transactional
    public BookingResponse bookClass(BookClassRequest request) {
        var user = userService.getLoginUser();
        var classSchedule = classScheduleRepository.findById(request.classId())
                .orElseThrow(() -> new NoSuchElementException("Class schedule not found with ID: " + request.classId()));

        var userPackage = userPackageRepository.findByIdAndUserIdAndStatus(request.userPackageId(), user.getId(), UserPackageStatus.ACTIVE)
                .orElseThrow(() -> new BaseException("Active user package not found for ID: " + request.userPackageId()));

        // --- Pre-booking Validations ---
        validateBookingPreconditions(user, userPackage, classSchedule);

        // --- Concurrency Control with Redis ---
        String slotsKey = "class:schedule:" + classSchedule.getId() + ":slots_available";
        // Initialize if not exists (e.g., first time a class is accessed, or after app restart)
        // This should ideally be initialized when a ClassSchedule is created/updated.
        long currentRedisSlots = stringRedisTemplate.opsForValue().get(slotsKey) != null ?
                Long.parseLong(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(slotsKey))) : -1; // If not initialized, treat as -1
        if (currentRedisSlots == -1) {
            // If Redis key not found, initialize it from DB capacity
            stringRedisTemplate.opsForValue().set(slotsKey, String.valueOf(classSchedule.getCapacity()));
            log.warn("Redis slot key {} was missing, initialized with capacity {}", slotsKey, classSchedule.getCapacity());
        }

        // Atomically decrement slots. If result is negative, it means it was overbooked.
        Long remainingSlots = stringRedisTemplate.opsForValue().decrement(slotsKey);

        if (remainingSlots != null && remainingSlots >= 0) {
            // Redis successfully reserved a slot. Proceed with database operations.
            try {
                // Deduct credits from user package
                userPackage.setCurrentCredits(userPackage.getCurrentCredits() - classSchedule.getRequiredCredits());
                // If credits drop to 0, mark as depleted
                if (userPackage.getCurrentCredits() <= 0) {
                    userPackage.setStatus(UserPackageStatus.DEPLETED);
                }
                userPackageRepository.save(userPackage); // Save changes to user package

                // Create booking record
                Booking newBooking = new Booking();
                newBooking.setUser(user);
                newBooking.setClassSchedule(classSchedule);
                newBooking.setUserPackage(userPackage);
                newBooking.setCreditsDeducted(classSchedule.getRequiredCredits());
                newBooking.setBookingTime(LocalDateTime.now());
                newBooking.setStatus(BookingStatus.CONFIRMED);
                var savedBooking = bookingRepository.save(newBooking);

                // Send confirmation email (mocked)
                notificationService.sendNotification(
                        new NotificationRequest(user.getEmail(), "Booking Confirm", classSchedule.getClassName())
                );

                log.info("User {} successfully booked class {}. Remaining Redis slots: {}", user.getId(), classSchedule.getId(), remainingSlots);
                return BookingResponse.from(savedBooking);

            } catch (Exception e) {
                // If any DB operation fails after Redis decrement, roll back Redis counter
                stringRedisTemplate.opsForValue().increment(slotsKey);
                log.error("Database transaction failed for booking user {} to class {}. Rolling back Redis. Error: {}", user.getId(), classSchedule.getId(), e.getMessage(), e);
                throw new BaseException("Booking failed due to database error. Please try again.");
            }
        } else {
            // Class is full or became full during concurrent attempt
            // Increment back the Redis counter if it went negative
            if (remainingSlots != null && remainingSlots < 0) {
                stringRedisTemplate.opsForValue().increment(slotsKey);
            }
            log.warn("User {} failed to book class {}. Class is full.", user.getId(), classSchedule.getId());
            throw new BaseException("Class is full. Please try again later or join the waitlist.");
        }
    }

    @Transactional
    public WaitListResponse addToWaitlist(BookClassRequest request) {
        var user = userService.getLoginUser();
        var classSchedule = classScheduleRepository.findById(request.classId())
                .orElseThrow(() -> new NoSuchElementException("Class schedule not found with ID: " + request.classId()));

        UserPackage userPackage = userPackageRepository.findByIdAndUserIdAndStatus(request.userPackageId(), user.getId(), UserPackageStatus.ACTIVE)
                .orElseThrow(() -> new BaseException("Active user package not found for ID: " + request.userPackageId()));

        // Validate booking preconditions, just like for a direct booking
        validateBookingPreconditions(user, userPackage, classSchedule);

        // Check if user is already on waitlist for this class
        if (waitlistRepository.existsByUserIdAndClassScheduleIdAndStatus(user.getId(), classSchedule.getId(), WaitListStatus.WAITING)) {
            throw new BaseException("You are already on the waitlist for this class.");
        }

        // It's assumed booking has failed and class is full when user attempts to join waitlist.
        // Credits are not deducted yet, but we ensure user has enough at the time of joining.
        if (userPackage.getCurrentCredits() < classSchedule.getRequiredCredits()) {
            throw new BaseException("Insufficient credits in your package to join the waitlist for this class.");
        }

        var newWaitlistEntry = new WaitList();
        newWaitlistEntry.setUser(user);
        newWaitlistEntry.setClassSchedule(classSchedule);
        newWaitlistEntry.setUserPackage(userPackage);
        newWaitlistEntry.setRequiredCreditsAtWaitlist(classSchedule.getRequiredCredits());
        newWaitlistEntry.setWaitlistTime(LocalDateTime.now());
        newWaitlistEntry.setStatus(WaitListStatus.WAITING);

        var savedWaitlistEntry = waitlistRepository.save(newWaitlistEntry);

        log.info("User {} added to waitlist for class {}", user.getId(), classSchedule.getId());
        return WaitListResponse.from(savedWaitlistEntry);
    }

    @Transactional
    public CancellationResponse cancelBooking(Long bookingId) {
        var user = userService.getLoginUser();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId + " for user: " + user.getId()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled.");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Cannot cancel a checked-in booking.");
        }

        var classSchedule = booking.getClassSchedule();
        UserPackage userPackage = booking.getUserPackage();

        // Calculate time difference to class start for refund policy
        LocalDateTime now = LocalDateTime.now();
        Duration durationUntilClass = Duration.between(now, classSchedule.getStartTime());
        long hoursUntilClass = durationUntilClass.toHours();

        boolean creditRefunded = false;
        if (hoursUntilClass >= 4) { // Refund if cancelled 4+ hours before class
            userPackage.setCurrentCredits(userPackage.getCurrentCredits() + booking.getCreditsDeducted());
            // If the package was depleted, set back to active
            if (userPackage.getStatus() == UserPackageStatus.DEPLETED && userPackage.getCurrentCredits() > 0) {
                userPackage.setStatus(UserPackageStatus.ACTIVE);
            }
            userPackageRepository.save(userPackage);
            creditRefunded = true;
            log.info("Credits ({}) refunded for booking ID {} due to early cancellation.", booking.getCreditsDeducted(), bookingId);
        } else {
            log.info("No credit refund for booking ID {} as cancellation is within 4 hours of class.", bookingId);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // --- Promote from Waitlist (FIFO) ---
        boolean waitlistPromoted = promoteWaitlistUser(classSchedule.getId());

        if(!waitlistPromoted) {
            String slotsKey = "class:schedule:" + classSchedule.getId() + ":slots_available";
            Long releasedSlots = stringRedisTemplate.opsForValue().increment(slotsKey);
            log.info("Slot released for class {}. New Redis slots: {}. Booking ID: {}", classSchedule.getId(), releasedSlots, bookingId);
        }

        // Send cancellation email (mocked)
        notificationService.sendNotification(new NotificationRequest(
                user.getEmail(), "Booking Cancelled",
                "Your booking for " + classSchedule.getClassName() + " at " + classSchedule.getStartTime() + " has been cancelled. " +
                        (creditRefunded ? "Credits have been refunded." : "No credits were refunded.")
        ));

        return new CancellationResponse("Booking cancelled successfully.", creditRefunded, waitlistPromoted);
    }

    @Transactional
    public BookingResponse checkInToClass(Long bookingId) {
        var user = userService.getLoginUser();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId + " for user: " + user.getId()));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BaseException("Cannot check in to a booking that is not confirmed or is already " + booking.getStatus());
        }

        var classSchedule = booking.getClassSchedule();
        var now = LocalDateTime.now();

        // Define check-in window (e.g., 30 mins before start, 15 mins after start)
        var checkInWindowStart = classSchedule.getStartTime().minusMinutes(30);
        var checkInWindowEnd = classSchedule.getStartTime().plusMinutes(15);

        if (now.isBefore(checkInWindowStart)) {
            throw new BaseException("Too early to check in for this class. Check-in opens at " + checkInWindowStart);
        }
        if (now.isAfter(checkInWindowEnd)) {
            throw new BaseException("Check-in window has closed for this class.");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckInTime(now);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("User {} checked into class {}. Booking ID: {}", user.getId(), classSchedule.getId(), bookingId);
        return BookingResponse.from(updatedBooking);
    }

    public List<BookingResponse> getMyBookings() {
        var user = userService.getLoginUser();
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        return bookings.stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    public List<WaitListResponse> getMyWaitlists() {
        var user = userService.getLoginUser();
        var waitlists = waitlistRepository.findByUserId(user.getId());
        return waitlists.stream()
                .map(WaitListResponse::from)
                .collect(Collectors.toList());
    }

    private void validateBookingPreconditions(User user, UserPackage userPackage, Clazz classSchedule) {
        // Check if class is in the past
        if (classSchedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BaseException("Cannot book a class that is in the past.");
        }

        // Package validity check (should be active)
        if (userPackage.getStatus() != UserPackageStatus.ACTIVE) {
            throw new BaseException("Your selected package is not active.");
        }

        // Country match check
        if (!userPackage.getCountryCode().equals(classSchedule.getCountryCode())) {
            throw new BaseException("Your package is for " + userPackage.getCountryCode() + " and cannot be used for a class in " + classSchedule.getCountryCode() + ".");
        }

        // Sufficient credits check
        if (userPackage.getCurrentCredits() < classSchedule.getRequiredCredits()) {
            throw new BaseException("You have " + userPackage.getCurrentCredits() + " credits, but this class requires " + classSchedule.getRequiredCredits() + " credits.");
        }

        // Overlapping booking check
        List<Clazz> overlappingClasses = classScheduleRepository.findOverlappingBookingsForUser(
                user.getId(), classSchedule.getStartTime(), classSchedule.getEndTime());
        if (!overlappingClasses.isEmpty()) {
            throw new BaseException("You already have a confirmed booking that overlaps with this class: " + overlappingClasses.get(0).getClassName() + " at " + overlappingClasses.get(0).getStartTime());
        }

        // Check if user already booked this class
        if (bookingRepository.existsByUserIdAndClassScheduleIdAndStatus(user.getId(), classSchedule.getId(), BookingStatus.CONFIRMED)) {
            throw new BaseException("You have already booked this class.");
        }
    }

    @Transactional
    public boolean promoteWaitlistUser(Long classScheduleId) {
        Optional<WaitList> oldestWaitingEntryOpt = waitlistRepository
                .findFirstByClassScheduleIdAndStatusOrderByWaitlistTimeAsc(classScheduleId, WaitListStatus.WAITING);

        if (oldestWaitingEntryOpt.isPresent()) {
            var waitlistEntry = oldestWaitingEntryOpt.get();
            var classSchedule = waitlistEntry.getClassSchedule();
            User user = waitlistEntry.getUser();
            UserPackage userPackage = waitlistEntry.getUserPackage();

            try {
                if (userPackage.getStatus() != UserPackageStatus.ACTIVE || userPackage.getCurrentCredits() < classSchedule.getRequiredCredits()) {
                    log.warn("Waitlist user {} for class {} cannot be promoted: Insufficient credits or inactive package. UserPackage ID: {}", user.getId(), classSchedule.getId(), userPackage.getId());
                    return false;
                }

                userPackage.setCurrentCredits(userPackage.getCurrentCredits() - classSchedule.getRequiredCredits());
                if (userPackage.getCurrentCredits() <= 0) {
                    userPackage.setStatus(UserPackageStatus.DEPLETED);
                }
                userPackageRepository.save(userPackage);

                Booking newBooking = new Booking();
                newBooking.setUser(user);
                newBooking.setClassSchedule(classSchedule);
                newBooking.setUserPackage(userPackage);
                newBooking.setCreditsDeducted(classSchedule.getRequiredCredits());
                newBooking.setBookingTime(LocalDateTime.now());
                newBooking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(newBooking);

                waitlistEntry.setStatus(WaitListStatus.BOOKED_FROM_WAITLIST);
                waitlistRepository.save(waitlistEntry);

                notificationService.sendNotification(new NotificationRequest(user.getEmail(), "WaitList Promotion" ,classSchedule.getClassName()));

                log.info("Waitlist user {} successfully promoted to booking for class {}. Booking ID: {}", user.getId(), classSchedule.getId(), newBooking.getId());
                return true;

            } catch (Exception e) {
                log.error("Failed to promote waitlist user {} for class {}: {}", user.getId(), classSchedule.getId(), e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

}
