package com.example.totpsender.controller;

import com.example.totpsender.config.ServiceFactory;
import com.example.totpsender.dto.OtpConfigRequest;
import com.example.totpsender.dto.OtpConfigResponse;
import com.example.totpsender.dto.ApiResponse;
import com.example.totpsender.dto.UserResponse;
import com.example.totpsender.model.OtpConfig;
import com.example.totpsender.model.User;
import com.example.totpsender.service.OtpConfigService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final OtpConfigService otpConfigService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AdminController(ServiceFactory serviceFactory) {
        this.userService = serviceFactory.getUserService();
        this.otpConfigService = serviceFactory.getOtpConfigService();
        this.otpService = serviceFactory.getOtpService();
        this.jwtUtil = serviceFactory.getJwtUtil();
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied"));
            }

            List<UserResponse> userResponses = userService.getAllUsers();

            logger.info("Retrieved {} users for admin", userResponses.size());
            return ResponseEntity.ok(userResponses);

        } catch (Exception e) {
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve users"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String id, HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied"));
            }

            UUID userId = UUID.fromString(id);
            User user = userService.findById(userId);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if ("ADMIN".equals(user.getRole())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Cannot delete admin user"));
            }

            userService.deleteUser(userId);
            otpService.deleteOtpCodesByUserId(userId);

            logger.info("User deleted successfully: {}", user.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid user ID format"));
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to delete user"));
        }
    }

    @GetMapping("/otp-config")
    public ResponseEntity<?> getOtpConfig(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied"));
            }

            OtpConfig config = otpConfigService.getOtpConfig();
            OtpConfigResponse response = new OtpConfigResponse(
                    config.getCodeLength(), config.getTtlSeconds());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving OTP config: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve OTP configuration"));
        }
    }

    @PutMapping("/otp-config")
    public ResponseEntity<ApiResponse> updateOtpConfig(@Valid @RequestBody OtpConfigRequest request,
                                                       HttpServletRequest httpRequest) {
        try {
            if (!isAdmin(httpRequest)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied"));
            }

            otpConfigService.updateOtpConfig(request.getCodeLength(), request.getTtlSeconds());

            logger.info("OTP configuration updated: length={}, ttl={}",
                    request.getCodeLength(), request.getTtlSeconds());
            return ResponseEntity.ok(new ApiResponse(true, "OTP configuration updated successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating OTP config: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update OTP configuration"));
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String role = jwtUtil.getRoleFromToken(token);
                return "ADMIN".equals(role);
            }
        }
        return false;
    }
}
