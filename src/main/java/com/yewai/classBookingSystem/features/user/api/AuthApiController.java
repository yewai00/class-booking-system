package com.yewai.classBookingSystem.features.user.api;

import com.yewai.classBookingSystem.common.dto.ApiResponse;
import com.yewai.classBookingSystem.features.user.api.request.*;
import com.yewai.classBookingSystem.features.user.api.response.AuthResponse;
import com.yewai.classBookingSystem.features.user.api.response.UserResponse;
import com.yewai.classBookingSystem.features.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Tag(name = "Authentication", description = "Authentication Api")
@RequestMapping("${api.version}")
public class AuthApiController {

    private final UserService userService;

    @PostMapping("/auth/register")
    @Operation(summary = "Signup User Api")
    public ResponseEntity<ApiResponse<UserResponse>> signupUser(@Valid @RequestBody UserRequest request) {
        var userResponse = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @GetMapping("/auth/verify-email")
    @Operation(summary = "Verify Email Api")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email Verified Success."));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login Api")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.login(request)));
    }

    @PostMapping("/auth/reset-password-request")
    @Operation(summary = "Password Reset Request Api")
    public ResponseEntity<ApiResponse<String>> resetPasswordRequest(@Valid @RequestBody PasswordResetReqRequest request) {
        userService.resetPasswordRequest(request.email());
        return ResponseEntity.ok(ApiResponse.success("Reset password token send to your email."));
    }

    @PostMapping("/auth/reset-password")
    @Operation(summary = "Password reset Api")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password Reset Successfully"));
    }
}
