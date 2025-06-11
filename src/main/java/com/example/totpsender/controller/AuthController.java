package com.example.totpsender.controller;

import com.example.totpsender.config.ServiceFactory;
import com.example.totpsender.dto.LoginRequest;
import com.example.totpsender.dto.RegisterRequest;
import com.example.totpsender.dto.AuthResponse;
import com.example.totpsender.model.User;
import com.example.totpsender.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(ServiceFactory serviceFactory) {
        this.authService = serviceFactory.getAuthService();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for username: {}", request.getUsername());

            boolean isAdmin = "ADMIN".equalsIgnoreCase(request.getRole());
            AuthResponse response = authService.register(request, isAdmin);

            logger.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            logger.warn("Registration failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Registration error for {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, null, "Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for username: {}", request.getUsername());

            AuthResponse response = authService.login(request);

            logger.info("User logged in successfully: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.warn("Login failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Login error for {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, null, "Login failed"));
        }
    }
}
