package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.models.JobRun;
import org.aburavov.pgbackupui.models.JobRunTriggerType;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    private final DbService dbService;
    private final StorageService storageService;
    private final ConnectionService connectionService;
    private final JobRunRepository jobRunRepository;

    public BackupService(DbService dbService, StorageService storageService,
                        ConnectionService connectionService,
                        JobRunRepository jobRunRepository) {
        this.dbService = dbService;
        this.storageService = storageService;
        this.connectionService = connectionService;
        this.jobRunRepository = jobRunRepository;
    }

    public String executeBackup(Job job) throws SQLException, IOException {
        return executeBackupInternal(job, JobRunTriggerType.MANUAL);
    }

    @Async
    public void executeScheduledBackup(Job job) {
        try {
            executeBackupInternal(job, JobRunTriggerType.SCHEDULED);
        } catch (Exception e) {
            logger.error("Scheduled backup failed for job {}: {}",
                        job.getName(), e.getMessage(), e);
        }
    }

    private String executeBackupInternal(Job job, JobRunTriggerType triggeredBy)
            throws SQLException, IOException {
        logger.info("Starting {} backup for job: {}", triggeredBy, job.getName());

        JobRun jobRun = new JobRun(job.getId(), triggeredBy);
        jobRun = jobRunRepository.save(jobRun);

        try {
            Connection connection = connectionService.findById(job.getConnectionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Connection not found: " + job.getConnectionId()));

            Storage storage = storageService.findById(job.getStorageId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Storage not found: " + job.getStorageId()));

            List<TableBackupData> backupDataList = dbService.extractTableData(job, connection);

            String backupPath = storageService.writeBackup(storage, backupDataList);

            storageService.applyRetentionPolicy(storage, job.getRetentionCount());

            Long totalSize = storageService.calculateDirectorySize(storage, backupPath);
            String folderName = Paths.get(backupPath).getFileName().toString();
            jobRun.markSuccess(folderName, totalSize);
            jobRunRepository.save(jobRun);

            logger.info("Completed {} backup for job: {}", triggeredBy, job.getName());
            return backupPath;

        } catch (Exception e) {
            jobRun.markFailed(e.getMessage());
            jobRunRepository.save(jobRun);
            throw e;
        }
    }
}
