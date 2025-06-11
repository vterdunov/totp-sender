package com.example.totpsender.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpValidateRequest {
    @NotBlank(message = "Code is required")
    private String code;

    private String operationId;

    public OtpValidateRequest() {
    }

    public OtpValidateRequest(String code) {
        this.code = code;
    }

    public OtpValidateRequest(String code, String operationId) {
        this.code = code;
        this.operationId = operationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
