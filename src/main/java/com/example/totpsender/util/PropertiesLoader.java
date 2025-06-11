package com.example.totpsender.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties loadProperties(String filename) {
        Properties properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream(filename)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + filename);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + filename, e);
        }
        return properties;
    }
}
