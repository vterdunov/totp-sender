package com.example.totpsender.service;

import com.example.totpsender.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        Properties config = PropertiesLoader.loadProperties("email.properties");
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void sendCode(String destination, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            Transport.send(message);
            logger.info("Email with OTP code sent successfully to: {}", destination);
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", destination, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        return username != null && password != null && fromEmail != null;
    }
}
