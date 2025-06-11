package com.example.totpsender.config;

import com.example.totpsender.util.PropertiesLoader;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration

public class DatabaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);
    private static DataSource dataSource;

    public DatabaseConfiguration() {
        // Public constructor required for Spring @Configuration
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return getDataSource();
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

        public static DataSource createDataSource() {
        logger.info("Initializing database connection...");

        Properties props = PropertiesLoader.loadProperties("database.properties");

        String dbUrl = props.getProperty("db.url");
        String dbUsername = props.getProperty("db.username");
        String dbPassword = props.getProperty("db.password");

        logger.info("Database properties loaded: url={}, username={}", dbUrl, dbUsername);

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(dbUrl);
        dataSource.setUser(dbUsername);
        dataSource.setPassword(dbPassword);

        logger.info("Database connection initialized successfully");
        return dataSource;
    }

    public static void closeDataSource() {
        logger.info("Database connection cleanup (no action needed for PGSimpleDataSource)");
    }
}
