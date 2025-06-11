package com.example.totpsender.repository;

import com.example.totpsender.model.OtpConfig;

import java.util.Optional;
import java.util.UUID;

public interface OtpConfigRepository extends Repository<OtpConfig, UUID> {

    Optional<OtpConfig> findFirst();
}
