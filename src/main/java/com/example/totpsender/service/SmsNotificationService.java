package com.example.totpsender.service;

import com.example.totpsender.util.PropertiesLoader;
import org.jsmpp.bean.*;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SmsNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties config = PropertiesLoader.loadProperties("sms.properties");
        this.host = config.getProperty("smpp.host");
        this.port = Integer.parseInt(config.getProperty("smpp.port"));
        this.systemId = config.getProperty("smpp.system_id");
        this.password = config.getProperty("smpp.password");
        this.systemType = config.getProperty("smpp.system_type");
        this.sourceAddress = config.getProperty("smpp.source_addr");
    }

        @Override
    public void sendCode(String destination, String code) {
        SMPPSession session = new SMPPSession();

        try {
            String systemIdResult = session.connectAndBind(host, port,
                new org.jsmpp.session.BindParameter(BindType.BIND_TX,
                    systemId, password, systemType,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));

            logger.info("Connected to SMPP server with system ID: {}", systemIdResult);

            String messageText = "Your code: " + code;
            session.submitShortMessage("",
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, sourceAddress,
                    TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, destination,
                    new ESMClass(), (byte) 0, (byte) 1, null, null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false),
                    (byte) 0, messageText.getBytes());

            logger.info("SMS with OTP code sent successfully to: {}", destination);

        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", destination, e);
            throw new RuntimeException("Failed to send SMS", e);
        } finally {
            session.unbindAndClose();
        }
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        return host != null && port > 0 && systemId != null && password != null;
    }
}
