package com.example.totpsender.repository.impl;

import com.example.totpsender.config.DatabaseConfiguration;
import com.example.totpsender.model.OtpConfig;
import com.example.totpsender.repository.OtpConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class OtpConfigRepositoryImpl implements OtpConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(OtpConfigRepositoryImpl.class);

    private static final String INSERT_CONFIG =
        "INSERT INTO otp_config (id, code_length, ttl_seconds, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_CONFIG =
        "UPDATE otp_config SET code_length = ?, ttl_seconds = ?, updated_at = ? WHERE id = ?";

    private static final String FIND_BY_ID =
        "SELECT id, code_length, ttl_seconds, created_at, updated_at FROM otp_config WHERE id = ?";

    private static final String FIND_FIRST =
        "SELECT id, code_length, ttl_seconds, created_at, updated_at FROM otp_config ORDER BY created_at LIMIT 1";

    private static final String FIND_ALL =
        "SELECT id, code_length, ttl_seconds, created_at, updated_at FROM otp_config ORDER BY created_at";

    private static final String DELETE_BY_ID = "DELETE FROM otp_config WHERE id = ?";

    private static final String EXISTS_BY_ID = "SELECT 1 FROM otp_config WHERE id = ?";

    @Override
    public OtpConfig save(OtpConfig config) {
        if (config.getId() == null) {
            config.setId(UUID.randomUUID());
            return insert(config);
        } else {
            return update(config);
        }
    }

    private OtpConfig insert(OtpConfig config) {
        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CONFIG)) {

            stmt.setObject(1, config.getId());
            stmt.setInt(2, config.getCodeLength());
            stmt.setInt(3, config.getTtlSeconds());
            stmt.setTimestamp(4, Timestamp.valueOf(config.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(config.getUpdatedAt()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to insert OTP config, no rows affected");
            }

            logger.debug("Inserted OTP config: length={}, ttl={}", config.getCodeLength(), config.getTtlSeconds());
            return config;

        } catch (SQLException e) {
            logger.error("Error inserting OTP config", e);
            throw new RuntimeException("Failed to insert OTP config", e);
        }
    }

    private OtpConfig update(OtpConfig config) {
        config.updateTimestamp();

        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CONFIG)) {

            stmt.setInt(1, config.getCodeLength());
            stmt.setInt(2, config.getTtlSeconds());
            stmt.setTimestamp(3, Timestamp.valueOf(config.getUpdatedAt()));
            stmt.setObject(4, config.getId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to update OTP config, no rows affected");
            }

            logger.debug("Updated OTP config: length={}, ttl={}", config.getCodeLength(), config.getTtlSeconds());
            return config;

        } catch (SQLException e) {
            logger.error("Error updating OTP config", e);
            throw new RuntimeException("Failed to update OTP config", e);
        }
    }

    @Override
    public Optional<OtpConfig> findById(UUID id) {
        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOtpConfig(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding OTP config by id: {}", id, e);
            throw new RuntimeException("Failed to find OTP config by id", e);
        }
    }

    @Override
    public Optional<OtpConfig> findFirst() {
        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_FIRST);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapResultSetToOtpConfig(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding first OTP config", e);
            throw new RuntimeException("Failed to find first OTP config", e);
        }
    }

    @Override
    public List<OtpConfig> findAll() {
        List<OtpConfig> configs = new ArrayList<>();

        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                configs.add(mapResultSetToOtpConfig(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding all OTP configs", e);
            throw new RuntimeException("Failed to find all OTP configs", e);
        }

        return configs;
    }

    @Override
    public void deleteById(UUID id) {
        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {

            stmt.setObject(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("No OTP config found with id: {}", id);
            } else {
                logger.debug("Deleted OTP config with id: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error deleting OTP config by id: {}", id, e);
            throw new RuntimeException("Failed to delete OTP config", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        try (Connection conn = DatabaseConfiguration.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if OTP config exists by id: {}", id, e);
            throw new RuntimeException("Failed to check OTP config existence", e);
        }
    }

    private OtpConfig mapResultSetToOtpConfig(ResultSet rs) throws SQLException {
        return new OtpConfig(
            (UUID) rs.getObject("id"),
            rs.getInt("code_length"),
            rs.getInt("ttl_seconds"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
