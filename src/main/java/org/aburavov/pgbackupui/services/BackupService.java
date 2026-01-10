package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.models.Storage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
public class BackupService {

    private final DbService dbService;
    private final StorageService storageService;
    private final ConnectionService connectionService;
    private final JobService jobService;

    public BackupService(DbService dbService, StorageService storageService,
                        ConnectionService connectionService, JobService jobService) {
        this.dbService = dbService;
        this.storageService = storageService;
        this.connectionService = connectionService;
        this.jobService = jobService;
    }

    public String executeBackup(Job job) throws SQLException, IOException {
        Connection connection = connectionService.findById(job.getConnectionId())
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + job.getConnectionId()));

        Storage storage = storageService.findById(job.getStorageId())
                .orElseThrow(() -> new IllegalArgumentException("Storage not found: " + job.getStorageId()));

        List<TableBackupData> backupDataList = dbService.extractTableData(job, connection);

        String backupPath = storageService.writeBackup(storage, backupDataList);

        return backupPath;
    }
}
