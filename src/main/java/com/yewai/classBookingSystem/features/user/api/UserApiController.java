package com.yewai.classBookingSystem.features.user.api;

import com.yewai.classBookingSystem.common.dto.ApiResponse;
import com.yewai.classBookingSystem.features.user.api.request.PasswordUpdateRequest;
import com.yewai.classBookingSystem.features.user.api.request.UserRequest;
import com.yewai.classBookingSystem.features.user.api.request.UserUpdateRequest;
import com.yewai.classBookingSystem.features.user.api.response.UserResponse;
import com.yewai.classBookingSystem.features.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "User", description = "User Api")
@AllArgsConstructor
@RequestMapping("${api.version}")
public class UserApiController {

    private final UserService userService;

    @GetMapping("/users/profile")
    @Operation(summary = "User Info Api")
    public ResponseEntity<ApiResponse<UserResponse>> getUser() {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser()));
    }

    @PutMapping("/users/profile")
    @Operation(summary = "Update User Info Api")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        var userResponse = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PutMapping("/users/change-password")
    @Operation(summary = "Update User Password Api")
    public ResponseEntity<ApiResponse<String>> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.passwordUpdate(request);
        return ResponseEntity.ok(ApiResponse.success("Password Updated Successfully"));
    }

}
