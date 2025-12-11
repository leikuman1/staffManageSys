package com.school045;

import com.school045.config.DatabaseConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTest {
    @Test
    void schemaContainsMandatoryObjects() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/schema.sql"));
        assertTrue(sql.contains("department045"));
        assertTrue(sql.contains("sp_department_title_counts045"));
        assertTrue(sql.contains("trg_staff_department_update045"));
    }

    @Test
    void defaultConfigurationTargets045Database() {
        DatabaseConfig config = new DatabaseConfig();
        assertTrue(config.url().contains("staffdb045"));
    }
}
