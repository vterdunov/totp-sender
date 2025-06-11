package com.example.totpsender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidateOtpRequest {

    @NotBlank(message = "Code is required")
    @Size(min = 4, max = 10, message = "Code must be between 4 and 10 characters")
    private String code;

    public ValidateOtpRequest() {}

    public ValidateOtpRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
