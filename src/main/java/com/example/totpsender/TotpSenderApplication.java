package com.example.totpsender;

import com.example.totpsender.config.DatabaseConfiguration;
import com.example.totpsender.repository.impl.OtpCodeRepositoryImpl;
import com.example.totpsender.service.ScheduledTaskService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@SpringBootApplication
public class TotpSenderApplication {

    private static final Logger logger = LoggerFactory.getLogger(TotpSenderApplication.class);
    private static ScheduledTaskService scheduledTaskService;

    public static void main(String[] args) {
        logger.info("Starting TOTP Sender Application...");

        try {
            ConfigurableApplicationContext context = SpringApplication.run(TotpSenderApplication.class, args);

            // Инициализация фоновых задач
            initializeScheduledTasks();

            // Добавляем shutdown hook для корректного завершения
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down TOTP Sender Application...");
                shutdownScheduledTasks();
                context.close();
                logger.info("TOTP Sender Application shutdown complete");
            }));

            logger.info("TOTP Sender Application started successfully");

        } catch (Exception e) {
            logger.error("Failed to start TOTP Sender Application", e);
            System.exit(1);
        }
    }

    private static void initializeScheduledTasks() {
        try {
            logger.info("Initializing scheduled tasks...");

            // Создаем DataSource и репозиторий для фоновых задач
            DataSource dataSource = DatabaseConfiguration.createDataSource();
            OtpCodeRepositoryImpl otpCodeRepository = new OtpCodeRepositoryImpl(dataSource);

            // Создаем и запускаем сервис планировщика
            scheduledTaskService = new ScheduledTaskService(otpCodeRepository);
            scheduledTaskService.start();

            logger.info("Scheduled tasks initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize scheduled tasks", e);
            throw new RuntimeException("Failed to initialize scheduled tasks", e);
        }
    }

    private static void shutdownScheduledTasks() {
        if (scheduledTaskService != null) {
            scheduledTaskService.shutdown();
        }
    }
}
