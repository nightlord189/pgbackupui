package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.models.JobRun;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

@Service
public class BackupService {

    private final DbService dbService;
    private final StorageService storageService;
    private final ConnectionService connectionService;
    private final JobService jobService;
    private final JobRunRepository jobRunRepository;

    public BackupService(DbService dbService, StorageService storageService,
                        ConnectionService connectionService, JobService jobService,
                        JobRunRepository jobRunRepository) {
        this.dbService = dbService;
        this.storageService = storageService;
        this.connectionService = connectionService;
        this.jobService = jobService;
        this.jobRunRepository = jobRunRepository;
    }

    public String executeBackup(Job job) throws SQLException, IOException {
        JobRun jobRun = new JobRun(job.getId(), "MANUAL");
        jobRun = jobRunRepository.save(jobRun);

        try {
            Connection connection = connectionService.findById(job.getConnectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + job.getConnectionId()));

            Storage storage = storageService.findById(job.getStorageId())
                    .orElseThrow(() -> new IllegalArgumentException("Storage not found: " + job.getStorageId()));

            List<TableBackupData> backupDataList = dbService.extractTableData(job, connection);

            String backupPath = storageService.writeBackup(storage, backupDataList);

            // Apply retention policy to clean up old backups
            storageService.applyRetentionPolicy(storage, job.getRetentionCount());

            Long totalSize = storageService.calculateDirectorySize(storage, backupPath);
            String folderName = Paths.get(backupPath).getFileName().toString();
            jobRun.markSuccess(folderName, totalSize);
            jobRunRepository.save(jobRun);

            return backupPath;
        } catch (Exception e) {
            jobRun.markFailed(e.getMessage());
            jobRunRepository.save(jobRun);
            throw e;
        }
    }
}
