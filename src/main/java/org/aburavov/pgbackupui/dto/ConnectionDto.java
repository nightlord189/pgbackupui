package org.aburavov.pgbackupui.dto;

import jakarta.validation.constraints.*;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.models.DatabaseType;

public class ConnectionDto {

    private String id;

    @NotBlank(message = "Connection name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Host is required")
    private String host;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer port;

    @NotBlank(message = "Database name is required")
    private String database;

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotNull(message = "Database type is required")
    private DatabaseType type = DatabaseType.POSTGRESQL;

    public ConnectionDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public static ConnectionDto from(Connection connection) {
        ConnectionDto dto = new ConnectionDto();
        dto.setId(connection.getId());
        dto.setName(connection.getName());
        dto.setHost(connection.getHost());
        dto.setPort(connection.getPort());
        dto.setDatabase(connection.getDatabase());
        dto.setUsername(connection.getUsername());
        dto.setType(connection.getType());
        return dto;
    }

    public Connection toEntity() {
        Connection connection = new Connection();
        connection.setName(this.name);
        connection.setHost(this.host);
        connection.setPort(this.port);
        connection.setDatabase(this.database);
        connection.setUsername(this.username);
        connection.setPassword(this.password);
        connection.setType(this.type);
        return connection;
    }
}
