package com.example.totpsender.service;

import com.example.totpsender.repository.OtpCodeRepository;
import com.example.totpsender.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTaskService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ScheduledExecutorService scheduler;
    private final OtpCleanupTask otpCleanupTask;
    private final int cleanupIntervalMinutes;

    public ScheduledTaskService(OtpCodeRepository otpCodeRepository) {
        Properties props = PropertiesLoader.loadProperties("scheduler.properties");
        this.cleanupIntervalMinutes = Integer.parseInt(props.getProperty("otp.cleanup.interval.minutes", "5"));

        this.scheduler = Executors.newScheduledThreadPool(1);
        this.otpCleanupTask = new OtpCleanupTask(otpCodeRepository);

        logger.info("ScheduledTaskService initialized with cleanup interval: {} minutes", cleanupIntervalMinutes);
    }

    public void start() {
        logger.info("Starting scheduled tasks...");

        // Запускаем задачу очистки OTP кодов с заданным интервалом
        scheduler.scheduleAtFixedRate(
            otpCleanupTask,
            1, // Первый запуск через 1 минуту после старта
            cleanupIntervalMinutes,
            TimeUnit.MINUTES
        );

        logger.info("OTP cleanup task scheduled to run every {} minutes", cleanupIntervalMinutes);
    }

    public void shutdown() {
        logger.info("Shutting down scheduled tasks...");
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Scheduler did not terminate gracefully, forcing shutdown");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for scheduler termination", e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Scheduled tasks shutdown complete");
    }
}
