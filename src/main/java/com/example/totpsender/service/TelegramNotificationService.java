package com.example.totpsender.service;

import com.example.totpsender.util.PropertiesLoader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final String botToken;
    private final String chatId;
    private final String telegramApiUrl;

    public TelegramNotificationService() {
        Properties config = PropertiesLoader.loadProperties("telegram.properties");
        this.botToken = config.getProperty("telegram.bot.token");
        this.chatId = config.getProperty("telegram.chat.id");
        String apiBaseUrl = config.getProperty("telegram.api.url");
        this.telegramApiUrl = apiBaseUrl + botToken + "/sendMessage";
    }

    @Override
    public void sendCode(String destination, String code) {
        String message = String.format(destination + ", your confirmation code is: %s", code);
        String url = String.format("%s?chat_id=%s&text=%s",
                telegramApiUrl,
                chatId,
                urlEncode(message));

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    logger.error("Telegram API error. Status code: {}", statusCode);
                    throw new RuntimeException("Telegram API error: " + statusCode);
                } else {
                    logger.info("Telegram message sent successfully");
                }
            }
        } catch (IOException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getChannelName() {
        return "TELEGRAM";
    }

    @Override
    public boolean isAvailable() {
        return botToken != null && chatId != null;
    }
}
