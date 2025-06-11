package com.example.totpsender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class TotpSenderApplication {

    private static final Logger logger = LoggerFactory.getLogger(TotpSenderApplication.class);

    public static void main(String[] args) {
        logger.info("Starting TOTP Sender Application...");
        SpringApplication.run(TotpSenderApplication.class, args);
        logger.info("TOTP Sender Application started successfully");
    }
}
