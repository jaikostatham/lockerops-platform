package com.jaico.lockerops.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

public final class FlywayMigrationRunner {

    private FlywayMigrationRunner() {
    }

    public static void main(String[] args) {
        String host = requiredEnvironmentVariable("DB_HOST");
        String port = environmentVariableOrDefault("DB_PORT", "5432");
        String database = requiredEnvironmentVariable("DB_NAME");
        String sslMode = environmentVariableOrDefault("DB_SSLMODE", "require");
        String username = requiredEnvironmentVariable("DB_MIGRATION_USERNAME");
        String password = requiredEnvironmentVariable("DB_MIGRATION_PASSWORD");
        String jdbcUrl = "jdbc:postgresql://%s:%s/%s?sslmode=%s".formatted(host, port, database, sslMode);

        Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1"))
                .load()
                .migrate();

        System.out.println("Flyway migration run completed.");
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }

    private static String environmentVariableOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
