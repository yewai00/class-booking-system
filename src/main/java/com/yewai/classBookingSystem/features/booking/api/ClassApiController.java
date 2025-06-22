package com.yewai.classBookingSystem.features.booking.api;

import com.yewai.classBookingSystem.common.dto.ApiResponse;
import com.yewai.classBookingSystem.features.booking.api.response.ClassResponse;
import com.yewai.classBookingSystem.features.booking.domain.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Class", description = "Class Api")
@RequestMapping("${api.version}")
public class ClassApiController {

    private final BookingService bookingService;

    @GetMapping("/classes")
    @Operation(summary = "Get Available classes Api")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAvailableClassSchedules(
            @RequestParam String countryCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var schedules = bookingService.getAvailableClassSchedules(countryCode, date);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

}
