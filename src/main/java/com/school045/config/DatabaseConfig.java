package com.school045.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/staffdb045?serverTimezone=UTC&allowMultiQueries=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_INIT = "false";

    private final Properties properties = new Properties();

    public DatabaseConfig() {
        loadFromResource();
        overrideFromEnvironment();
        applyDefaults();
    }

    private void loadFromResource() {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
            // Use defaults/environment if loading fails
        }
    }

    private void overrideFromEnvironment() {
        setIfPresent("db.url", System.getenv("DB_URL"));
        setIfPresent("db.user", System.getenv("DB_USER"));
        setIfPresent("db.password", System.getenv("DB_PASSWORD"));
        setIfPresent("db.initialize", System.getenv("DB_INITIALIZE"));
    }

    private void applyDefaults() {
        properties.putIfAbsent("db.url", DEFAULT_URL);
        properties.putIfAbsent("db.user", DEFAULT_USER);
        properties.putIfAbsent("db.password", DEFAULT_PASSWORD);
        properties.putIfAbsent("db.initialize", DEFAULT_INIT);
    }

    private void setIfPresent(String key, String value) {
        Optional.ofNullable(value).filter(v -> !v.isBlank()).ifPresent(v -> properties.setProperty(key, v));
    }

    public String url() {
        return properties.getProperty("db.url");
    }

    public String user() {
        return properties.getProperty("db.user");
    }

    public String password() {
        return properties.getProperty("db.password");
    }

    public boolean shouldInitializeSchema() {
        return Boolean.parseBoolean(properties.getProperty("db.initialize", DEFAULT_INIT));
    }

    public Properties asProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }
}
