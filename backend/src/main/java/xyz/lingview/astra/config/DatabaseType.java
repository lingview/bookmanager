package xyz.lingview.astra.config;

public enum DatabaseType {

    MYSQL("mysql", "classpath:db/migration/mysql"),
    SQLITE("sqlite", "classpath:db/migration/sqlite");

    private final String code;
    private final String migrationLocation;

    DatabaseType(String code, String migrationLocation) {
        this.code = code;
        this.migrationLocation = migrationLocation;
    }

    public boolean matches(String value) {
        return value != null && code.equalsIgnoreCase(value.trim());
    }

    public String migrationLocation() {
        return migrationLocation;
    }

    public static DatabaseType fromUrl(String url) {
        return url != null && url.startsWith("jdbc:sqlite:") ? SQLITE : MYSQL;
    }
}