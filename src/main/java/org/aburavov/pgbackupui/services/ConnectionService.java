package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.ColumnSchema;
import org.aburavov.pgbackupui.dto.TableSchema;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.repositories.ConnectionRepository;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;

    public ConnectionService(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    public List<Connection> findAll() {
        return connectionRepository.findAll();
    }

    public Optional<Connection> findById(String id) {
        return connectionRepository.findById(id);
    }

    public Optional<Connection> findByName(String name) {
        return connectionRepository.findByName(name);
    }

    public Connection create(Connection connection) {
        if (connectionRepository.existsByName(connection.getName())) {
            throw new IllegalArgumentException(
                "Connection with name '" + connection.getName() + "' already exists"
            );
        }
        return connectionRepository.save(connection);
    }

    public Connection update(String id, Connection connection) {
        Connection existing = connectionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));

        if (!existing.getName().equals(connection.getName())
            && connectionRepository.existsByName(connection.getName())) {
            throw new IllegalArgumentException(
                "Connection with name '" + connection.getName() + "' already exists"
            );
        }

        existing.setName(connection.getName());
        existing.setType(connection.getType());
        existing.setHost(connection.getHost());
        existing.setPort(connection.getPort());
        existing.setDatabase(connection.getDatabase());
        existing.setUsername(connection.getUsername());
        if (connection.getPassword() != null && !connection.getPassword().isBlank()) {
            existing.setPassword(connection.getPassword());
        }
        existing.updateTimestamp();

        return connectionRepository.save(existing);
    }

    public void delete(String id) {
        if (!connectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Connection not found: " + id);
        }
        connectionRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return connectionRepository.existsByName(name);
    }

    public List<TableSchema> getDatabaseSchema(Connection connection) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                connection.getHost(),
                connection.getPort(),
                connection.getDatabase());

        List<TableSchema> tables = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection(
                url,
                connection.getUsername(),
                connection.getPassword())) {

            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet tablesRs = metaData.getTables(
                    connection.getDatabase(),
                    "public",
                    null,
                    new String[]{"TABLE"})) {

                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");
                    List<ColumnSchema> columns = getColumnsForTable(metaData, connection.getDatabase(), tableName);
                    tables.add(new TableSchema(tableName, columns));
                }
            }
        }

        return tables;
    }

    private List<ColumnSchema> getColumnsForTable(DatabaseMetaData metaData, String database, String tableName)
            throws SQLException {
        List<ColumnSchema> columns = new ArrayList<>();

        try (ResultSet columnsRs = metaData.getColumns(database, "public", tableName, null)) {
            while (columnsRs.next()) {
                String columnName = columnsRs.getString("COLUMN_NAME");
                String columnType = columnsRs.getString("TYPE_NAME");
                boolean nullable = columnsRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;

                columns.add(new ColumnSchema(columnName, columnType, nullable));
            }
        }

        return columns;
    }
}
