package com.example.totpsender.repository;

import com.example.totpsender.model.OtpCode;
import com.example.totpsender.model.OtpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends Repository<OtpCode, UUID> {

    Optional<OtpCode> findByCode(String code);

    List<OtpCode> findExpiredCodes();

    void updateStatus(UUID id, OtpStatus status);

    void deleteByUserId(UUID userId);

    List<OtpCode> findByUserIdAndOperationId(UUID userId, String operationId);
}
