package com.yewai.classBookingSystem.features.user.domain.service;

import com.yewai.classBookingSystem.exception.BaseException;
import com.yewai.classBookingSystem.features.notification.api.request.NotificationRequest;
import com.yewai.classBookingSystem.features.notification.domain.service.NotificationService;
import com.yewai.classBookingSystem.features.user.api.request.*;
import com.yewai.classBookingSystem.features.user.api.response.AuthResponse;
import com.yewai.classBookingSystem.features.user.api.response.UserResponse;
import com.yewai.classBookingSystem.features.user.domain.entity.User;
import com.yewai.classBookingSystem.features.user.domain.repo.UserRepository;
import com.yewai.classBookingSystem.utils.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper mapper;

    public UserResponse getUser() {
        var user = this.getLoginUser();
        return UserResponse.from(user);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchElementException("Invalid UserName or Password."));
        if (!user.getIsEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email.");
        }
        var jwtToken = jwtTokenProvider.generateToken(user);
        var refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return new AuthResponse(jwtToken, refreshToken);
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use!");
        }
        var user = mapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        var saveduser = userRepository.save(user);
        var verificationLink = "/auth/verify-email?token=" + saveduser.getVerificationToken();
        notificationService.sendNotification(new NotificationRequest(
                saveduser.getEmail(),
                "Email Verification",
                verificationLink
        ));
        return UserResponse.from(saveduser);
    }

    public UserResponse updateUser(UserUpdateRequest request) {
        var user = this.getLoginUser();
        var oldPassword = user.getPassword();
        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new BaseException("Email is already in use!");
        }
        user.setEmail(request.email());
        user.setName(request.name());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        } else {
            user.setPassword(oldPassword);
        }
        var updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }

    public void verifyEmail(String token) {
        var user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void resetPasswordRequest(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Invalid email"));
        var resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        userRepository.save(user);
        var resetLink = "Your Reset password token: " + resetToken;
        notificationService.sendNotification(new NotificationRequest(
                user.getEmail(),
                "Password Reset",
                resetLink
        ));
    }

    public void resetPassword(PasswordResetRequest request) {
        var user = userRepository.findByResetPasswordToken(request.token())
                .orElseThrow(() -> new NoSuchElementException("Invalid email"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    public void passwordUpdate(PasswordUpdateRequest request) {
        var user = this.getLoginUser();
        if (passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BaseException("Current Password incorrect!.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new BaseException("Fail to retrieve login user"));
        }
        return null;
    }
}
