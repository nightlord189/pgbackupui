package org.aburavov.pgbackupui.dto;

import java.util.List;

public class TableBackupData {
    private String tableName;
    private List<String> sqlStatements;

    public TableBackupData() {
    }

    public TableBackupData(String tableName, List<String> sqlStatements) {
        this.tableName = tableName;
        this.sqlStatements = sqlStatements;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getSqlStatements() {
        return sqlStatements;
    }

    public void setSqlStatements(List<String> sqlStatements) {
        this.sqlStatements = sqlStatements;
    }
}
