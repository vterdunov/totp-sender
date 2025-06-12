package com.example.totpsender.controller;

import com.example.totpsender.config.ServiceFactory;
import com.example.totpsender.dto.GenerateOtpRequest;
import com.example.totpsender.dto.ValidateOtpRequest;
import com.example.totpsender.dto.OtpResponse;
import com.example.totpsender.model.OtpCode;
import com.example.totpsender.model.User;
import com.example.totpsender.dto.OtpGenerateRequest;
import com.example.totpsender.dto.OtpValidateRequest;
import com.example.totpsender.service.NotificationService;
import com.example.totpsender.service.OtpService;
import com.example.totpsender.service.UserService;
import com.example.totpsender.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/user/otp")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final OtpService otpService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ServiceFactory serviceFactory;

    public UserController(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
        this.otpService = serviceFactory.getOtpService();
        this.userService = serviceFactory.getUserService();
        this.jwtUtil = serviceFactory.getJwtUtil();
    }

    @PostMapping("/generate")
    public ResponseEntity<OtpResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request,
                                                   HttpServletRequest httpRequest) {
        try {
            String username = extractUsernameFromToken(httpRequest);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new OtpResponse(false, "Invalid or missing token"));
            }

            logger.info("Generating OTP for user: {} with destination: {} via channel: {}",
                    username, request.getDestination(), request.getChannel());

            // Get user by username
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new OtpResponse(false, "User not found"));
            }

            // Generate OTP code
            OtpGenerateRequest otpRequest = new OtpGenerateRequest(
                    request.getOperationId(), request.getChannel(), request.getDestination());
            OtpCode otpCode = otpService.generateOtp(user, otpRequest);

            // Send notification based on channel
            NotificationService notificationService = getNotificationService(request.getChannel());
            if (notificationService == null || !notificationService.isAvailable()) {
                return ResponseEntity.badRequest()
                        .body(new OtpResponse(false, "Notification channel not available: " + request.getChannel()));
            }

            notificationService.sendCode(request.getDestination(), otpCode.getCode());

            logger.info("OTP generated and sent successfully for user: {}", username);
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent successfully"));

        } catch (Exception e) {
            logger.error("Error generating OTP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OtpResponse(false, "Failed to generate OTP"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<OtpResponse> validateOtp(@Valid @RequestBody ValidateOtpRequest request,
                                                   HttpServletRequest httpRequest) {
        try {
            String username = extractUsernameFromToken(httpRequest);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new OtpResponse(false, "Invalid or missing token"));
            }

            logger.info("Validating OTP for user: {} with code: {}", username, request.getCode());

            // Get user by username
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new OtpResponse(false, "User not found"));
            }

            // Validate OTP code
            OtpValidateRequest otpValidateRequest = new OtpValidateRequest(request.getCode());
            boolean isValid = otpService.validateOtp(user, otpValidateRequest);

            if (isValid) {
                logger.info("OTP validated successfully for user: {}", username);
                return ResponseEntity.ok(new OtpResponse(true, "OTP is valid"));
            } else {
                logger.warn("Invalid OTP attempt for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(new OtpResponse(false, "Invalid or expired OTP"));
            }

        } catch (Exception e) {
            logger.error("Error validating OTP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OtpResponse(false, "Failed to validate OTP"));
        }
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUsernameFromToken(token);
            }
        }
        return null;
    }

    private NotificationService getNotificationService(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return serviceFactory.getEmailNotificationService();
            case "SMS":
                return serviceFactory.getSmsNotificationService();
            case "TELEGRAM":
                return serviceFactory.getTelegramNotificationService();
            case "FILE":
                return serviceFactory.getFileNotificationService();
            default:
                return null;
        }
    }
}
