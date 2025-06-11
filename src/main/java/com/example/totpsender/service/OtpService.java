package com.example.totpsender.service;

import com.example.totpsender.dto.OtpGenerateRequest;
import com.example.totpsender.dto.OtpValidateRequest;
import com.example.totpsender.model.OtpCode;
import com.example.totpsender.model.OtpConfig;
import com.example.totpsender.model.OtpStatus;
import com.example.totpsender.model.User;
import com.example.totpsender.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeRepository otpCodeRepository;
    private final OtpConfigService otpConfigService;
    private final SecureRandom secureRandom;

    public OtpService(OtpCodeRepository otpCodeRepository, OtpConfigService otpConfigService) {
        this.otpCodeRepository = otpCodeRepository;
        this.otpConfigService = otpConfigService;
        this.secureRandom = new SecureRandom();
    }

    public OtpCode generateOtp(User user, OtpGenerateRequest request) {
        logger.info("Generating OTP for user: {} operation: {}",
                   user.getUsername(), request.getOperationId());

        OtpConfig config = otpConfigService.getCurrentConfig();

        // Generate random code
        String code = generateRandomCode(config.getCodeLength());

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getTtlSeconds());

        // Create OTP code
        OtpCode otpCode = new OtpCode(
            user.getId(),
            code,
            request.getOperationId(),
            expiresAt
        );

        // Save to database
        otpCode = otpCodeRepository.save(otpCode);

        logger.info("Generated OTP code for user: {} operation: {} expires: {}",
                   user.getUsername(), request.getOperationId(), expiresAt);

        return otpCode;
    }

    public boolean validateOtp(User user, OtpValidateRequest request) {
        logger.info("Validating OTP for user: {} operation: {}",
                   user.getUsername(), request.getOperationId());

        // Find OTP code
        Optional<OtpCode> otpCodeOpt = otpCodeRepository.findByCode(request.getCode());
        if (otpCodeOpt.isEmpty()) {
            logger.warn("OTP code not found: {}", request.getCode());
            return false;
        }

        OtpCode otpCode = otpCodeOpt.get();

        // Validate ownership
        if (!otpCode.getUserId().equals(user.getId())) {
            logger.warn("OTP code does not belong to user: {} code: {}",
                       user.getUsername(), request.getCode());
            return false;
        }

        // Validate operation ID
        if (!request.getOperationId().equals(otpCode.getOperationId())) {
            logger.warn("OTP code operation ID mismatch: expected: {} actual: {}",
                       request.getOperationId(), otpCode.getOperationId());
            return false;
        }

        // Check if code is active and not expired
        if (!otpCode.isActive()) {
            logger.warn("OTP code is not active: {} status: {}",
                       request.getCode(), otpCode.getStatus());
            return false;
        }

        // Mark as used
        otpCode.markAsUsed();
        otpCodeRepository.save(otpCode);

        logger.info("Successfully validated OTP for user: {} operation: {}",
                   user.getUsername(), request.getOperationId());

        return true;
    }

    public void markExpiredCodes() {
        logger.debug("Marking expired OTP codes");

        List<OtpCode> expiredCodes = otpCodeRepository.findExpiredCodes();

        for (OtpCode code : expiredCodes) {
            code.markAsExpired();
            otpCodeRepository.save(code);
        }

        if (!expiredCodes.isEmpty()) {
            logger.info("Marked {} OTP codes as expired", expiredCodes.size());
        }
    }

    public void validateChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel is required");
        }

        String upperChannel = channel.toUpperCase();
        if (!upperChannel.equals("EMAIL") &&
            !upperChannel.equals("SMS") &&
            !upperChannel.equals("TELEGRAM") &&
            !upperChannel.equals("FILE")) {
            throw new IllegalArgumentException("Invalid channel: " + channel +
                ". Supported channels: EMAIL, SMS, TELEGRAM, FILE");
        }
    }

    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
