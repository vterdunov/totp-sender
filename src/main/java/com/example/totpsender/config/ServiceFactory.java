package com.example.totpsender.config;

import com.example.totpsender.repository.OtpCodeRepository;
import com.example.totpsender.repository.OtpConfigRepository;
import com.example.totpsender.repository.UserRepository;
import com.example.totpsender.repository.impl.OtpCodeRepositoryImpl;
import com.example.totpsender.repository.impl.OtpConfigRepositoryImpl;
import com.example.totpsender.repository.impl.UserRepositoryImpl;
import com.example.totpsender.service.*;
import com.example.totpsender.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

    // Repositories
    private final UserRepository userRepository;
    private final OtpConfigRepository otpConfigRepository;
    private final OtpCodeRepository otpCodeRepository;

    // Utilities
    private final JwtUtil jwtUtil;

    // Services
    private final AuthService authService;
    private final UserService userService;
    private final OtpConfigService otpConfigService;
    private final OtpService otpService;

    public ServiceFactory(DataSource dataSource) {
        logger.info("Initializing ServiceFactory...");
        this.userRepository = new UserRepositoryImpl(dataSource);
        this.otpConfigRepository = new OtpConfigRepositoryImpl(dataSource);
        this.otpCodeRepository = new OtpCodeRepositoryImpl(dataSource);
        this.jwtUtil = new JwtUtil();
        this.otpConfigService = new OtpConfigService(otpConfigRepository);
        this.authService = new AuthService(userRepository, jwtUtil);
        this.userService = new UserService(userRepository, otpCodeRepository);
        this.otpService = new OtpService(otpCodeRepository, otpConfigService);
        logger.info("ServiceFactory initialized successfully");
    }

    // Repository getters
    public UserRepository getUserRepository() {
        return userRepository;
    }

    public OtpConfigRepository getOtpConfigRepository() {
        return otpConfigRepository;
    }

    public OtpCodeRepository getOtpCodeRepository() {
        return otpCodeRepository;
    }

    // Utility getters
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    // Service getters
    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public OtpConfigService getOtpConfigService() {
        return otpConfigService;
    }

    public OtpService getOtpService() {
        return otpService;
    }

    // Cleanup resources
    public void shutdown() {
        logger.info("Shutting down ServiceFactory...");
        DatabaseConfiguration.closeDataSource();
        logger.info("ServiceFactory shutdown complete");
    }

    public NotificationService getEmailNotificationService() {
        return new EmailNotificationService();
    }

    public NotificationService getSmsNotificationService() {
        return new SmsNotificationService();
    }

    public NotificationService getTelegramNotificationService() {
        return new TelegramNotificationService();
    }

    public NotificationService getFileNotificationService() {
        return new FileNotificationService();
    }
}
