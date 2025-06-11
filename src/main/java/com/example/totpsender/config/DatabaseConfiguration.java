package com.example.totpsender.config;

import com.example.totpsender.util.PropertiesLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class DatabaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);
    private static DataSource dataSource;

    private DatabaseConfiguration() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    public static DataSource createDataSource() {
        logger.info("Initializing database connection pool...");

        Properties props = PropertiesLoader.loadProperties("database.properties");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));

        // Pool settings
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

        // Pool name for logging
        config.setPoolName("TotpSenderPool");

        logger.info("Database connection pool initialized successfully");
        return new HikariDataSource(config);
    }

    public static void closeDataSource() {
        if (dataSource instanceof HikariDataSource) {
            logger.info("Closing database connection pool...");
            ((HikariDataSource) dataSource).close();
            logger.info("Database connection pool closed");
        }
    }
}
