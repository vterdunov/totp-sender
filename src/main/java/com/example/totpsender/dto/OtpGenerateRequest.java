package com.example.totpsender.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpGenerateRequest {
    @NotBlank(message = "Operation ID is required")
    private String operationId;

    @NotBlank(message = "Channel is required")
    private String channel; // EMAIL, SMS, TELEGRAM, FILE

    private String destination; // email, phone, telegram chat, or file path

    public OtpGenerateRequest() {
    }

    public OtpGenerateRequest(String operationId, String channel, String destination) {
        this.operationId = operationId;
        this.channel = channel;
        this.destination = destination;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
