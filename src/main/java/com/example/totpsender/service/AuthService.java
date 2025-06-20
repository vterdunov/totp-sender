package com.example.totpsender.service;

import com.example.totpsender.dto.AuthResponse;
import com.example.totpsender.dto.LoginRequest;
import com.example.totpsender.dto.RegisterRequest;
import com.example.totpsender.exception.AdminAlreadyExistsException;
import com.example.totpsender.exception.InvalidCredentialsException;
import com.example.totpsender.exception.UserAlreadyExistsException;
import com.example.totpsender.exception.UserNotFoundException;
import com.example.totpsender.model.User;
import com.example.totpsender.model.UserRole;
import com.example.totpsender.repository.UserRepository;
import com.example.totpsender.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResponse register(RegisterRequest request, boolean isAdmin) {
        logger.info("Attempting to register user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        UserRole role = isAdmin ? UserRole.ADMIN : UserRole.USER;

        // Check if admin already exists (only one admin allowed)
        if (role == UserRole.ADMIN && userRepository.existsByRole(UserRole.ADMIN)) {
            throw new AdminAlreadyExistsException("Admin user already exists. Only one admin is allowed.");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create and save user
        User user = new User(request.getUsername(), hashedPassword, role);
        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        logger.info("Successfully registered user: {} with role: {}", user.getUsername(), user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting to login user: {}", request.getUsername());

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Failed login attempt for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        logger.info("Successfully logged in user: {} with role: {}", user.getUsername(), user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public String getRoleFromToken(String token) {
        return jwtUtil.getRoleFromToken(token);
    }

    public String login(String username, String password) {
        logger.info("Attempting to login user: {}", username);

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Failed login attempt for user: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        logger.info("Successfully logged in user: {} with role: {}", user.getUsername(), user.getRole());

        return token;
    }

    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public User findUserByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }
        return userOpt.get();
    }
}
