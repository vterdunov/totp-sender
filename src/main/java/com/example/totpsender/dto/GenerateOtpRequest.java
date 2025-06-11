package com.example.totpsender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class GenerateOtpRequest {

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "^(EMAIL|SMS|TELEGRAM|FILE)$", message = "Channel must be one of: EMAIL, SMS, TELEGRAM, FILE")
    private String channel;

    private String operationId;

    public GenerateOtpRequest() {}

    public GenerateOtpRequest(String destination, String channel, String operationId) {
        this.destination = destination;
        this.channel = channel;
        this.operationId = operationId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
