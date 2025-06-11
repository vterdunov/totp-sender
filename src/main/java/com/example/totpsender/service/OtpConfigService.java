package com.example.totpsender.service;

import com.example.totpsender.dto.OtpConfigRequest;
import com.example.totpsender.model.OtpConfig;
import com.example.totpsender.repository.OtpConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OtpConfigService {

    private static final Logger logger = LoggerFactory.getLogger(OtpConfigService.class);

    private final OtpConfigRepository otpConfigRepository;

    public OtpConfigService(OtpConfigRepository otpConfigRepository) {
        this.otpConfigRepository = otpConfigRepository;
    }

    public OtpConfig getCurrentConfig() {
        Optional<OtpConfig> configOpt = otpConfigRepository.findFirst();
        if (configOpt.isEmpty()) {
            // Create default config if none exists
            logger.info("No OTP config found, creating default configuration");
            return createDefaultConfig();
        }
        return configOpt.get();
    }

    public OtpConfig updateConfig(OtpConfigRequest request) {
        logger.info("Updating OTP configuration: length={}, ttl={}",
                   request.getCodeLength(), request.getTtlSeconds());

        Optional<OtpConfig> existingConfigOpt = otpConfigRepository.findFirst();

        OtpConfig config;
        if (existingConfigOpt.isPresent()) {
            // Update existing config
            config = existingConfigOpt.get();
            config.setCodeLength(request.getCodeLength());
            config.setTtlSeconds(request.getTtlSeconds());
            config.updateTimestamp();
        } else {
            // Create new config
            config = new OtpConfig(request.getCodeLength(), request.getTtlSeconds());
        }

        config = otpConfigRepository.save(config);

        logger.info("Successfully updated OTP configuration: length={}, ttl={}",
                   config.getCodeLength(), config.getTtlSeconds());

        return config;
    }

    public void validateConfig(OtpConfigRequest request) {
        if (request.getCodeLength() < 4 || request.getCodeLength() > 8) {
            throw new IllegalArgumentException("Code length must be between 4 and 8");
        }

        if (request.getTtlSeconds() < 30 || request.getTtlSeconds() > 3600) {
            throw new IllegalArgumentException("TTL must be between 30 and 3600 seconds");
        }
    }

    private OtpConfig createDefaultConfig() {
        OtpConfig defaultConfig = new OtpConfig(6, 300); // 6 digits, 5 minutes
        return otpConfigRepository.save(defaultConfig);
    }
}
