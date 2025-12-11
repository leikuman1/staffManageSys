package com.school045.db;

import com.school045.config.DatabaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private final DatabaseConfig config;

    public Database(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    public void initializeSchemaIfNeeded() {
        if (!config.shouldInitializeSchema()) {
            return;
        }
        try (Connection connection = getConnection()) {
            for (String statement : loadSchemaStatements()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(statement);
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to initialize schema", e);
        }
    }

    private List<String> loadSchemaStatements() throws IOException {
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schema.sql")) {
            if (stream == null) {
                throw new IOException("schema.sql not found on classpath");
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return splitStatements(sql);
        }
    }

    private List<String> splitStatements(String sql) {
        String[] raw = sql.split("(?m);\\s*(?=\\n|$)");
        List<String> statements = new ArrayList<>();
        for (String statement : raw) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            statements.add(trimmed);
        }
        return statements;
    }
}
