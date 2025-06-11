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

public class ServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

    // Singleton instance
    private static ServiceFactory instance;

    // Repositories
    private UserRepository userRepository;
    private OtpConfigRepository otpConfigRepository;
    private OtpCodeRepository otpCodeRepository;

    // Utilities
    private JwtUtil jwtUtil;

    // Services
    private AuthService authService;
    private UserService userService;
    private OtpConfigService otpConfigService;
    private OtpService otpService;

    private ServiceFactory() {
        logger.info("Initializing ServiceFactory...");
        initializeComponents();
        logger.info("ServiceFactory initialized successfully");
    }

    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    private void initializeComponents() {
        // Initialize repositories
        userRepository = new UserRepositoryImpl();
        otpConfigRepository = new OtpConfigRepositoryImpl();
        otpCodeRepository = new OtpCodeRepositoryImpl();

        // Initialize utilities
        jwtUtil = new JwtUtil();

        // Initialize services with dependencies
        authService = new AuthService(userRepository, jwtUtil);
        userService = new UserService(userRepository, otpCodeRepository);
        otpConfigService = new OtpConfigService(otpConfigRepository);
        otpService = new OtpService(otpCodeRepository, otpConfigService);
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

    // For testing - allows resetting the singleton
    public static void reset() {
        instance = null;
    }

    // Cleanup resources
    public void shutdown() {
        logger.info("Shutting down ServiceFactory...");
        DatabaseConfiguration.closeDataSource();
        logger.info("ServiceFactory shutdown complete");
    }
}
