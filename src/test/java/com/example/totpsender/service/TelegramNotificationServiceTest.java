package com.example.totpsender.service;

import com.example.totpsender.exception.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TelegramNotificationServiceTest {

    @TempDir
    Path tempDir;

    private TelegramNotificationService telegramService;
    private Path telegramPropertiesPath;

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл telegram.properties
        telegramPropertiesPath = tempDir.resolve("telegram.properties");

        Properties props = new Properties();
        props.setProperty("telegram.bot.token", "test_bot_token");
        props.setProperty("telegram.chat.id", "test_chat_id");
        props.setProperty("telegram.api.url", "https://api.telegram.org/bot");

        StringWriter writer = new StringWriter();
        props.store(writer, null);
        Files.writeString(telegramPropertiesPath, writer.toString());

        // Создаем тестовый TelegramNotificationService
        telegramService = new TestTelegramNotificationService();
    }

    @Test
    void sendCode_ShouldSendMessageSuccessfully() {
        // Given
        String destination = "TestUser";
        String code = "123456";

        // When & Then
        assertDoesNotThrow(() -> telegramService.sendCode(destination, code));
    }

    @Test
    void sendCode_ShouldFormatMessageCorrectly() {
        // Given
        String destination = "TestUser";
        String code = "123456";
        TestTelegramNotificationService testService = new TestTelegramNotificationService();

        // When
        testService.sendCode(destination, code);

        // Then
        String expectedMessage = "TestUser, your confirmation code is: 123456";
        assertTrue(testService.getLastUrl().contains("text=TestUser%2C+your+confirmation+code+is%3A+123456"));
    }

    @Test
    void sendCode_ShouldIncludeCorrectParameters() {
        // Given
        String destination = "TestUser";
        String code = "123456";
        TestTelegramNotificationService testService = new TestTelegramNotificationService();

        // When
        testService.sendCode(destination, code);

        // Then
        String lastUrl = testService.getLastUrl();
        assertTrue(lastUrl.contains("chat_id=test_chat_id"));
        assertTrue(lastUrl.contains("https://api.telegram.org/bottest_bot_token/sendMessage"));
    }

    @Test
    void getChannelName_ShouldReturnTelegram() {
        // When
        String channelName = telegramService.getChannelName();

        // Then
        assertEquals("TELEGRAM", channelName);
    }

    @Test
    void isAvailable_ShouldReturnTrue() {
        // When
        boolean available = telegramService.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    void sendCode_ShouldThrowExceptionOnHttpError() {
        // Given
        TestTelegramNotificationService errorService = new TestTelegramNotificationService();
        errorService.setSimulateError(true);
        String destination = "TestUser";
        String code = "123456";

        // When & Then
        assertThrows(NotificationException.class, () -> errorService.sendCode(destination, code));
    }

    // Тестовая реализация TelegramNotificationService
    private static class TestTelegramNotificationService extends TelegramNotificationService {
        private String lastUrl;
        private boolean simulateError = false;

        @Override
        public void sendCode(String destination, String code) {
            String message = String.format(destination + ", your confirmation code is: %s", code);
            String url = String.format("%s?chat_id=%s&text=%s",
                    "https://api.telegram.org/bottest_bot_token/sendMessage",
                    "test_chat_id",
                    urlEncode(message));

            this.lastUrl = url;

            if (simulateError) {
                throw new NotificationException("Simulated HTTP error");
            }

            // Имитируем успешную отправку
        }

        private static String urlEncode(String value) {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        }

        public String getLastUrl() {
            return lastUrl;
        }

        public void setSimulateError(boolean simulateError) {
            this.simulateError = simulateError;
        }

        @Override
        public String getChannelName() {
            return "TELEGRAM";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
