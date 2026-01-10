package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.ColumnSchema;
import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.dto.TableSchema;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.models.Job;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DbService {

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

    public List<TableBackupData> extractTableData(Job job, Connection connection) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                connection.getHost(),
                connection.getPort(),
                connection.getDatabase());

        List<Job.TableConfig> tablesToBackup;

        if (job.getTables() == null || job.getTables().isEmpty()) {
            tablesToBackup = getAllTables(connection);
        } else {
            tablesToBackup = job.getTables();
        }

        List<TableBackupData> backupDataList = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection(
                url,
                connection.getUsername(),
                connection.getPassword())) {

            for (Job.TableConfig tableConfig : tablesToBackup) {
                TableBackupData tableData = extractTableBackupData(conn, tableConfig);
                backupDataList.add(tableData);
            }
        }

        return backupDataList;
    }

    private List<Job.TableConfig> getAllTables(Connection connection) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                connection.getHost(),
                connection.getPort(),
                connection.getDatabase());

        List<Job.TableConfig> tables = new ArrayList<>();

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
                    tables.add(new Job.TableConfig(tableName, new ArrayList<>()));
                }
            }
        }

        return tables;
    }

    private TableBackupData extractTableBackupData(java.sql.Connection conn, Job.TableConfig tableConfig) throws SQLException {
        String tableName = tableConfig.getTableName();
        List<String> columns = tableConfig.getColumns();

        String columnList;
        if (columns == null || columns.isEmpty()) {
            columnList = "*";
        } else {
            columnList = columns.stream()
                    .map(col -> "\"" + col + "\"")
                    .collect(Collectors.joining(", "));
        }

        String selectQuery = String.format("SELECT %s FROM \"%s\"", columnList, tableName);

        List<String> sqlStatements = new ArrayList<>();

        sqlStatements.add("-- Backup of table: " + tableName);
        sqlStatements.add("-- Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        sqlStatements.add("");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectQuery)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                StringBuilder insertStatement = new StringBuilder();
                insertStatement.append("INSERT INTO \"").append(tableName).append("\" (");

                List<String> columnNames = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add("\"" + metaData.getColumnName(i) + "\"");
                }
                insertStatement.append(String.join(", ", columnNames));
                insertStatement.append(") VALUES (");

                List<String> values = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        values.add("NULL");
                    } else if (value instanceof String) {
                        String strValue = value.toString().replace("'", "''");
                        values.add("'" + strValue + "'");
                    } else if (value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                        values.add("'" + value.toString() + "'");
                    } else if (value instanceof Boolean) {
                        values.add(((Boolean) value) ? "TRUE" : "FALSE");
                    } else {
                        values.add(value.toString());
                    }
                }
                insertStatement.append(String.join(", ", values));
                insertStatement.append(");");

                sqlStatements.add(insertStatement.toString());
            }
        }

        return new TableBackupData(tableName, sqlStatements);
    }
}
