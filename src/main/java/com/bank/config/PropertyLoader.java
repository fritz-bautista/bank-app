package com.bank.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    private static final String PROPERTIES_FILE = "application.properties";
    public static volatile PropertyLoader instance;
    private final Properties properties;

    private PropertyLoader() {
        properties = new Properties();
        loadProperties();
    }

    public static PropertyLoader getInstance() {
        if (instance == null) {
            synchronized (PropertyLoader.class) {
                if (instance == null) {
                    instance = new PropertyLoader();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new RuntimeException(
                        "Configuration file not found!"
                );
            }
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read '" + PROPERTIES_FILE + "': " + e.getMessage(), e);
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException(
                    "Required property not found"
            );
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? value.trim() : defaultValue;
    }
}
