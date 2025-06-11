package com.example.totpsender.repository.impl;

import com.example.totpsender.config.DatabaseConfiguration;
import com.example.totpsender.model.User;
import com.example.totpsender.model.UserRole;
import com.example.totpsender.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final DataSource dataSource;

    private static final String INSERT_USER =
        "INSERT INTO users (id, username, password_hash, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_USER =
        "UPDATE users SET username = ?, password_hash = ?, role = ?, updated_at = ? WHERE id = ?";

    private static final String FIND_BY_ID =
        "SELECT id, username, password_hash, role, created_at, updated_at FROM users WHERE id = ?";

    private static final String FIND_BY_USERNAME =
        "SELECT id, username, password_hash, role, created_at, updated_at FROM users WHERE username = ?";

    private static final String FIND_ALL =
        "SELECT id, username, password_hash, role, created_at, updated_at FROM users ORDER BY created_at";

    private static final String FIND_ALL_EXCEPT_ADMINS =
        "SELECT id, username, password_hash, role, created_at, updated_at FROM users WHERE role != 'ADMIN' ORDER BY created_at";

    private static final String DELETE_BY_ID = "DELETE FROM users WHERE id = ?";

    private static final String EXISTS_BY_ID = "SELECT 1 FROM users WHERE id = ?";

    private static final String EXISTS_BY_ROLE = "SELECT 1 FROM users WHERE role = ?";

    private static final String COUNT_BY_ROLE = "SELECT COUNT(*) FROM users WHERE role = ?";

    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {

            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to insert user, no rows affected");
            }

            logger.debug("Inserted user: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error inserting user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    private User update(User user) {
        user.updateTimestamp();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getUpdatedAt()));
            stmt.setObject(5, user.getId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to update user, no rows affected");
            }

            logger.debug("Updated user: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USERNAME)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Failed to find user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        return executeQuery(FIND_ALL);
    }

    @Override
    public List<User> findAllExceptAdmins() {
        return executeQuery(FIND_ALL_EXCEPT_ADMINS);
    }

    private List<User> executeQuery(String sql) {
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }

        return users;
    }

    @Override
    public void deleteById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {

            stmt.setObject(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("No user found with id: {}", id);
            } else {
                logger.debug("Deleted user with id: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error deleting user by id: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
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
            logger.error("Error checking if user exists by id: {}", id, e);
            throw new RuntimeException("Failed to check user existence", e);
        }
    }

    @Override
    public boolean existsByRole(UserRole role) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_ROLE)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if user exists by role: {}", role, e);
            throw new RuntimeException("Failed to check user existence by role", e);
        }
    }

    @Override
    public long countByRole(UserRole role) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_ROLE)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            logger.error("Error counting users by role: {}", role, e);
            throw new RuntimeException("Failed to count users by role", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            (UUID) rs.getObject("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            UserRole.valueOf(rs.getString("role")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
