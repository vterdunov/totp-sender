package com.example.totpsender.service;

import com.example.totpsender.util.PropertiesLoader;
import org.opensmpp.Session;
import org.opensmpp.TCPIPConnection;
import org.opensmpp.pdu.BindResponse;
import org.opensmpp.pdu.BindTransmitter;
import org.opensmpp.pdu.SubmitSM;
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
        TCPIPConnection connection = null;
        Session session = null;

        try {
            connection = new TCPIPConnection(host, port);
            session = new Session(connection);

            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34);
            bindRequest.setAddressRange(sourceAddress);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new RuntimeException("Bind failed: " + bindResponse.getCommandStatus());
            }

            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr(sourceAddress);
            submitSM.setDestAddr(destination);
            submitSM.setShortMessage("Your code: " + code);

            session.submit(submitSM);
            logger.info("SMS with OTP code sent successfully to: {}", destination);

        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", destination, e);
            throw new RuntimeException("Failed to send SMS", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    logger.warn("Failed to close SMPP session", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.warn("Failed to close SMPP connection", e);
                }
            }
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
