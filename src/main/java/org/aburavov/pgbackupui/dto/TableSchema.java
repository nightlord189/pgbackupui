package org.aburavov.pgbackupui.dto;

import java.util.List;

public class TableSchema {

    private String tableName;
    private List<ColumnSchema> columns;

    public TableSchema() {
    }

    public TableSchema(String tableName, List<ColumnSchema> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnSchema> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSchema> columns) {
        this.columns = columns;
    }
}
