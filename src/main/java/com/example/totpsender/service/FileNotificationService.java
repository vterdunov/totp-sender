package com.example.totpsender.service;

import com.example.totpsender.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class FileNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String outputDirectory;
    private final String filename;
    private final boolean appendMode;

    public FileNotificationService() {
        Properties config = PropertiesLoader.loadProperties("file.properties");
        this.outputDirectory = config.getProperty("file.output.directory");
        this.filename = config.getProperty("file.output.filename");
        this.appendMode = Boolean.parseBoolean(config.getProperty("file.append.mode", "true"));
    }

    @Override
    public void sendCode(String destination, String code) {
        try {
            Path filePath = Paths.get(outputDirectory, filename);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logEntry = String.format("[%s] Destination: %s, Code: %s%n", timestamp, destination, code);

            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), appendMode))) {
                writer.print(logEntry);
                writer.flush();
            }

            logger.info("OTP code saved to file for destination: {}", destination);
        } catch (IOException e) {
            logger.error("Failed to save OTP code to file for destination: {}", destination, e);
            throw new RuntimeException("Failed to save OTP code to file", e);
        }
    }

    @Override
    public String getChannelName() {
        return "FILE";
    }

    @Override
    public boolean isAvailable() {
        return outputDirectory != null && filename != null;
    }
}
