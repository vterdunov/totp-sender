package com.example.totpsender.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OtpConfigRequest {
    @NotNull(message = "Code length is required")
    @Min(value = 4, message = "Code length must be at least 4")
    @Max(value = 8, message = "Code length must be at most 8")
    private Integer codeLength;

    @NotNull(message = "TTL seconds is required")
    @Min(value = 30, message = "TTL must be at least 30 seconds")
    @Max(value = 3600, message = "TTL must be at most 3600 seconds")
    private Integer ttlSeconds;

    public OtpConfigRequest() {
    }

    public OtpConfigRequest(Integer codeLength, Integer ttlSeconds) {
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
    }

    public Integer getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public Integer getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Integer ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
