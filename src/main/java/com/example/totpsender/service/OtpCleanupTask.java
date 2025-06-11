package com.example.totpsender.service;

import com.example.totpsender.model.OtpCode;
import com.example.totpsender.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OtpCleanupTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(OtpCleanupTask.class);

    private final OtpCodeRepository otpCodeRepository;

    public OtpCleanupTask(OtpCodeRepository otpCodeRepository) {
        this.otpCodeRepository = otpCodeRepository;
    }

    @Override
    public void run() {
        try {
            logger.debug("Starting OTP cleanup task...");

            // Получаем просроченные коды
            List<OtpCode> expiredCodes = otpCodeRepository.findExpiredCodes();

            if (!expiredCodes.isEmpty()) {
                logger.info("Found {} expired OTP codes to clean up", expiredCodes.size());

                // Обновляем статус на EXPIRED
                for (OtpCode code : expiredCodes) {
                    otpCodeRepository.updateStatus(code.getId(), OtpCode.Status.EXPIRED);
                }

                logger.info("Successfully updated {} OTP codes to EXPIRED status", expiredCodes.size());
            } else {
                logger.debug("No expired OTP codes found");
            }

        } catch (Exception e) {
            logger.error("Error during OTP cleanup task execution", e);
        }
    }
}
