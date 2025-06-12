package com.example.totpsender.service;

import com.example.totpsender.dto.OtpGenerateRequest;
import com.example.totpsender.model.OtpCode;
import com.example.totpsender.model.OtpConfig;
import com.example.totpsender.model.User;
import com.example.totpsender.model.UserRole;
import com.example.totpsender.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;
    private User testUser;
    private OtpConfig testConfig;
    private OtpGenerateRequest testRequest;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        testUser = new User("testuser", "hashedpassword", UserRole.USER);
        testUser.setId(UUID.randomUUID());

        testConfig = new OtpConfig(6, 300); // 6 digits, 300 seconds TTL
        testRequest = new OtpGenerateRequest("test-operation", "EMAIL", "test@example.com");

        // Создаем mock репозитория и конфиг сервиса
        TestOtpCodeRepository mockRepository = new TestOtpCodeRepository();
        TestOtpConfigService mockConfigService = new TestOtpConfigService(testConfig);

        // Создаем сервис с тестовыми зависимостями
        otpService = new OtpService(mockRepository, mockConfigService);
    }

    @Test
    void generateOtp_ShouldGenerateCodeWithCorrectLength() {
        // When
        OtpCode result = otpService.generateOtp(testUser, testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertEquals(6, result.getCode().length());
    }

    @Test
    void generateOtp_ShouldGenerateNumericCode() {
        // When
        OtpCode result = otpService.generateOtp(testUser, testRequest);

        // Then
        assertNotNull(result.getCode());
        assertTrue(result.getCode().matches("\\d+"), "Code should contain only digits");
    }

    @Test
    void generateOtp_ShouldGenerateDifferentCodes() {
        // When
        OtpCode result1 = otpService.generateOtp(testUser, testRequest);
        OtpCode result2 = otpService.generateOtp(testUser, testRequest);

        // Then
        assertNotEquals(result1.getCode(), result2.getCode(), "Generated codes should be different");
    }

    @Test
    void generateOtp_ShouldGenerateCodeWithCustomLength() {
        // Given
        OtpConfig customConfig = new OtpConfig(4, 300); // 4 digits
        TestOtpConfigService customConfigService = new TestOtpConfigService(customConfig);
        OtpService customOtpService = new OtpService(new TestOtpCodeRepository(), customConfigService);

        // When
        OtpCode result = customOtpService.generateOtp(testUser, testRequest);

        // Then
        assertEquals(4, result.getCode().length());
        assertTrue(result.getCode().matches("\\d{4}"), "Code should be exactly 4 digits");
    }

    @Test
    void generateOtp_ShouldSetCorrectUserIdAndOperationId() {
        // When
        OtpCode result = otpService.generateOtp(testUser, testRequest);

        // Then
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testRequest.getOperationId(), result.getOperationId());
        assertNotNull(result.getExpiresAt());
    }

    // Простые тестовые классы
    private static class TestOtpCodeRepository implements OtpCodeRepository {
        @Override
        public OtpCode save(OtpCode otpCode) {
            if (otpCode.getId() == null) {
                otpCode.setId(UUID.randomUUID());
            }
            return otpCode;
        }

        @Override
        public java.util.Optional<OtpCode> findByCode(String code) {
            return java.util.Optional.empty();
        }

        @Override
        public java.util.List<OtpCode> findExpiredCodes() {
            return java.util.Collections.emptyList();
        }

        @Override
        public void deleteByUserId(UUID userId) {
            // No implementation needed for this test
        }

        @Override
        public void updateStatus(java.util.UUID id, com.example.totpsender.model.OtpStatus status) {
            // No implementation needed for this test
        }

        @Override
        public java.util.List<OtpCode> findByUserIdAndOperationId(UUID userId, String operationId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.Optional<OtpCode> findById(UUID id) {
            return java.util.Optional.empty();
        }

        @Override
        public java.util.List<OtpCode> findAll() {
            return java.util.Collections.emptyList();
        }

        @Override
        public void deleteById(UUID id) {
            // No implementation needed for this test
        }

        @Override
        public boolean existsById(UUID id) {
            return false;
        }
    }

    private static class TestOtpConfigService extends OtpConfigService {
        private final OtpConfig config;

        public TestOtpConfigService(OtpConfig config) {
            super(null); // Передаем null в родительский конструктор
            this.config = config;
        }

        @Override
        public OtpConfig getCurrentConfig() {
            return config;
        }
    }
}
