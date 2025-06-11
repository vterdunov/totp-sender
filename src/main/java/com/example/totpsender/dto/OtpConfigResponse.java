package com.example.totpsender.dto;

public class OtpConfigResponse {

    private Integer codeLength;
    private Integer ttlSeconds;

    public OtpConfigResponse() {}

    public OtpConfigResponse(Integer codeLength, Integer ttlSeconds) {
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
