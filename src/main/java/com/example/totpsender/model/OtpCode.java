package com.example.totpsender.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class OtpCode {
    private UUID id;
    private UUID userId;
    private String code;
    private String operationId;
    private OtpStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public OtpCode() {
    }

    public OtpCode(UUID userId, String code, String operationId, LocalDateTime expiresAt) {
        this.userId = userId;
        this.code = code;
        this.operationId = operationId;
        this.status = OtpStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public OtpCode(UUID id, UUID userId, String code, String operationId,
                   OtpStatus status, LocalDateTime createdAt, LocalDateTime expiresAt,
                   LocalDateTime usedAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.operationId = operationId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == OtpStatus.ACTIVE && !isExpired();
    }

    public void markAsUsed() {
        this.status = OtpStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = OtpStatus.EXPIRED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtpCode otpCode = (OtpCode) o;
        return Objects.equals(id, otpCode.id) && Objects.equals(code, otpCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", userId=" + userId +
                ", code='" + code + '\'' +
                ", operationId='" + operationId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", usedAt=" + usedAt +
                '}';
    }
}
