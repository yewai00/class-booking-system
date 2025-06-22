package com.yewai.classBookingSystem.features.booking.api;

import com.yewai.classBookingSystem.common.dto.ApiResponse;
import com.yewai.classBookingSystem.features.booking.api.request.BookClassRequest;
import com.yewai.classBookingSystem.features.booking.api.response.BookingResponse;
import com.yewai.classBookingSystem.features.booking.api.response.CancellationResponse;
import com.yewai.classBookingSystem.features.booking.api.response.WaitListResponse;
import com.yewai.classBookingSystem.features.booking.domain.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Booking", description = "Booking Api")
@RequestMapping("${api.version}")
public class BookingApiController {

    private final BookingService bookingService;

    @PostMapping("/bookings/book")
    @Operation(summary = "Book class Api")
    public ResponseEntity<ApiResponse<BookingResponse>> bookClass(
            @Valid @RequestBody BookClassRequest request
            ) {
        var booking = bookingService.bookClass(request);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PostMapping("/bookings/waitlist")
    @Operation(summary = "Add to waitlist Api")
    public ResponseEntity<ApiResponse<WaitListResponse>> waitlistClass(
            @Valid @RequestBody BookClassRequest request
    ) {
        var waitlistEntry = bookingService.addToWaitlist(request);
        return ResponseEntity.ok(ApiResponse.success(waitlistEntry));
    }

    @PostMapping("/bookings/{id}/cancel")
    @Operation(summary = "Cancel Booking Api")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelBooking(
            @PathVariable("id") Long bookingId
    ) {
        var response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bookings/my-bookings")
    @Operation(summary = "Get Booking list Api")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        var bookings = bookingService.getMyBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PostMapping("/bookings/{id}/check-in")
    @Operation(summary = "Check in Api")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(
            @PathVariable("id") Long bookingId
    ) {
        var checkedInBooking = bookingService.checkInToClass(bookingId);
        return ResponseEntity.ok(ApiResponse.success(checkedInBooking));
    }

}

