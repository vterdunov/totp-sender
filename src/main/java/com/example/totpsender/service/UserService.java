package com.example.totpsender.service;

import com.example.totpsender.dto.UserResponse;
import com.example.totpsender.exception.AccessDeniedException;
import com.example.totpsender.exception.UserNotFoundException;
import com.example.totpsender.model.User;
import com.example.totpsender.model.UserRole;
import com.example.totpsender.repository.UserRepository;
import com.example.totpsender.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;

    public UserService(UserRepository userRepository, OtpCodeRepository otpCodeRepository) {
        this.userRepository = userRepository;
        this.otpCodeRepository = otpCodeRepository;
    }

    public User findByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }
        return userOpt.get();
    }

    public User findById(UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return userOpt.get();
    }

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users except admins");

        List<User> users = userRepository.findAllExceptAdmins();

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(UUID userId) {
        logger.info("Attempting to delete user with id: {}", userId);

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        // Delete associated OTP codes first (cascade delete)
        otpCodeRepository.deleteByUserId(userId);
        logger.debug("Deleted OTP codes for user: {}", userId);

        // Delete user
        userRepository.deleteById(userId);

        logger.info("Successfully deleted user with id: {}", userId);
    }

    public void validateAdminAccess(String role) {
        if (!UserRole.ADMIN.name().equals(role)) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
    }

    public void validateUserAccess(String role) {
        if (!UserRole.USER.name().equals(role) && !UserRole.ADMIN.name().equals(role)) {
            throw new AccessDeniedException("Access denied. User role required.");
        }
    }

    public boolean isAdmin(String role) {
        return UserRole.ADMIN.name().equals(role);
    }

    public boolean isUser(String role) {
        return UserRole.USER.name().equals(role);
    }

    public long countUsers() {
        return userRepository.countByRole(UserRole.USER);
    }

    public long countAdmins() {
        return userRepository.countByRole(UserRole.ADMIN);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole().name(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
