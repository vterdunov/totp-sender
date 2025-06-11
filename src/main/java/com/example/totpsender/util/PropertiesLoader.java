package com.example.totpsender.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream inputStream = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Property file '" + fileName + "' not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + fileName, e);
        }
        return properties;
    }
}
