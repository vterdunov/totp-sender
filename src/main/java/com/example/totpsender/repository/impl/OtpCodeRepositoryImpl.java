package com.example.totpsender.repository.impl;

import com.example.totpsender.config.DatabaseConfiguration;
import com.example.totpsender.model.OtpCode;
import com.example.totpsender.model.OtpStatus;
import com.example.totpsender.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OtpCodeRepositoryImpl implements OtpCodeRepository {

    private static final Logger logger = LoggerFactory.getLogger(OtpCodeRepositoryImpl.class);
    private final DataSource dataSource;

    private static final String INSERT_CODE =
        "INSERT INTO otp_codes (id, user_id, code, operation_id, status, created_at, expires_at, used_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_CODE =
        "UPDATE otp_codes SET user_id = ?, code = ?, operation_id = ?, status = ?, expires_at = ?, used_at = ? WHERE id = ?";

    private static final String FIND_BY_ID =
        "SELECT id, user_id, code, operation_id, status, created_at, expires_at, used_at FROM otp_codes WHERE id = ?";

    private static final String FIND_BY_CODE =
        "SELECT id, user_id, code, operation_id, status, created_at, expires_at, used_at FROM otp_codes WHERE code = ?";

    private static final String FIND_BY_USER_ID_AND_OPERATION_ID =
        "SELECT id, user_id, code, operation_id, status, created_at, expires_at, used_at FROM otp_codes WHERE user_id = ? AND operation_id = ? ORDER BY created_at DESC";

    private static final String FIND_ALL =
        "SELECT id, user_id, code, operation_id, status, created_at, expires_at, used_at FROM otp_codes ORDER BY created_at DESC";

    private static final String FIND_EXPIRED_CODES =
        "SELECT id, user_id, code, operation_id, status, created_at, expires_at, used_at FROM otp_codes WHERE status = 'ACTIVE' AND expires_at < ?";

    private static final String UPDATE_STATUS = "UPDATE otp_codes SET status = ? WHERE id = ?";

    private static final String DELETE_BY_ID = "DELETE FROM otp_codes WHERE id = ?";

    private static final String DELETE_BY_USER_ID = "DELETE FROM otp_codes WHERE user_id = ?";

    private static final String EXISTS_BY_ID = "SELECT 1 FROM otp_codes WHERE id = ?";

    public OtpCodeRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public OtpCode save(OtpCode otpCode) {
        if (otpCode.getId() == null) {
            otpCode.setId(UUID.randomUUID());
            return insert(otpCode);
        } else {
            return update(otpCode);
        }
    }

    private OtpCode insert(OtpCode otpCode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CODE)) {

            stmt.setObject(1, otpCode.getId());
            stmt.setObject(2, otpCode.getUserId());
            stmt.setString(3, otpCode.getCode());
            stmt.setString(4, otpCode.getOperationId());
            stmt.setString(5, otpCode.getStatus().name());
            stmt.setTimestamp(6, Timestamp.valueOf(otpCode.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(otpCode.getExpiresAt()));

            if (otpCode.getUsedAt() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(otpCode.getUsedAt()));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to insert OTP code, no rows affected");
            }

            logger.debug("Inserted OTP code: {}", otpCode.getCode());
            return otpCode;

        } catch (SQLException e) {
            logger.error("Error inserting OTP code: {}", otpCode.getCode(), e);
            throw new RuntimeException("Failed to insert OTP code", e);
        }
    }

    private OtpCode update(OtpCode otpCode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CODE)) {

            stmt.setObject(1, otpCode.getUserId());
            stmt.setString(2, otpCode.getCode());
            stmt.setString(3, otpCode.getOperationId());
            stmt.setString(4, otpCode.getStatus().name());
            stmt.setTimestamp(5, Timestamp.valueOf(otpCode.getExpiresAt()));

            if (otpCode.getUsedAt() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(otpCode.getUsedAt()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setObject(7, otpCode.getId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to update OTP code, no rows affected");
            }

            logger.debug("Updated OTP code: {}", otpCode.getCode());
            return otpCode;

        } catch (SQLException e) {
            logger.error("Error updating OTP code: {}", otpCode.getCode(), e);
            throw new RuntimeException("Failed to update OTP code", e);
        }
    }

    @Override
    public Optional<OtpCode> findById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOtpCode(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding OTP code by id: {}", id, e);
            throw new RuntimeException("Failed to find OTP code by id", e);
        }
    }

    @Override
    public Optional<OtpCode> findByCode(String code) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_CODE)) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOtpCode(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding OTP code by code: {}", code, e);
            throw new RuntimeException("Failed to find OTP code by code", e);
        }
    }

    @Override
    public List<OtpCode> findByUserIdAndOperationId(UUID userId, String operationId) {
        List<OtpCode> codes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USER_ID_AND_OPERATION_ID)) {

            stmt.setObject(1, userId);
            stmt.setString(2, operationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codes.add(mapResultSetToOtpCode(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding OTP codes by user id and operation id: {} {}", userId, operationId, e);
            throw new RuntimeException("Failed to find OTP codes by user id and operation id", e);
        }

        return codes;
    }

    @Override
    public List<OtpCode> findExpiredCodes() {
        List<OtpCode> codes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_EXPIRED_CODES)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codes.add(mapResultSetToOtpCode(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding expired OTP codes", e);
            throw new RuntimeException("Failed to find expired OTP codes", e);
        }

        return codes;
    }

    @Override
    public List<OtpCode> findAll() {
        List<OtpCode> codes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                codes.add(mapResultSetToOtpCode(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding all OTP codes", e);
            throw new RuntimeException("Failed to find all OTP codes", e);
        }

        return codes;
    }

    @Override
    public void updateStatus(UUID id, OtpStatus status) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {

            stmt.setString(1, status.name());
            stmt.setObject(2, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("No OTP code found with id: {}", id);
            } else {
                logger.debug("Updated OTP code status to {} for id: {}", status, id);
            }

        } catch (SQLException e) {
            logger.error("Error updating OTP code status for id: {}", id, e);
            throw new RuntimeException("Failed to update OTP code status", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {

            stmt.setObject(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("No OTP code found with id: {}", id);
            } else {
                logger.debug("Deleted OTP code with id: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error deleting OTP code by id: {}", id, e);
            throw new RuntimeException("Failed to delete OTP code", e);
        }
    }

    @Override
    public void deleteByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_USER_ID)) {

            stmt.setObject(1, userId);

            int affected = stmt.executeUpdate();
            logger.debug("Deleted {} OTP codes for user id: {}", affected, userId);

        } catch (SQLException e) {
            logger.error("Error deleting OTP codes by user id: {}", userId, e);
            throw new RuntimeException("Failed to delete OTP codes by user id", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if OTP code exists by id: {}", id, e);
            throw new RuntimeException("Failed to check OTP code existence", e);
        }
    }

    private OtpCode mapResultSetToOtpCode(ResultSet rs) throws SQLException {
        Timestamp usedAtTimestamp = rs.getTimestamp("used_at");
        LocalDateTime usedAt = usedAtTimestamp != null ? usedAtTimestamp.toLocalDateTime() : null;

        return new OtpCode(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("user_id"),
            rs.getString("code"),
            rs.getString("operation_id"),
            OtpStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime(),
            usedAt
        );
    }
}
