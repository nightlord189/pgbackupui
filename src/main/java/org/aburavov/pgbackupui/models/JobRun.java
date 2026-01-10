package org.aburavov.pgbackupui.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "job_runs")
public class JobRun {

    @Id
    private String id;

    @NotBlank(message = "Job ID is required")
    @Indexed
    private String jobId;

    @NotNull(message = "Started at is required")
    @Indexed
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @NotBlank(message = "Status is required")
    private String status; // RUNNING, SUCCESS, FAILED

    private String errorMessage;

    private String triggeredBy; // MANUAL, SCHEDULED

    private String folderName; // Name of the backup folder

    private Long filesSize; // Total size of backup files in bytes

    public JobRun() {
        this.startedAt = LocalDateTime.now();
        this.status = "RUNNING";
    }

    public JobRun(String jobId, String triggeredBy) {
        this();
        this.jobId = jobId;
        this.triggeredBy = triggeredBy;
    }

    public void markSuccess(String folderName, Long filesSize) {
        this.status = "SUCCESS";
        this.finishedAt = LocalDateTime.now();
        this.folderName = folderName;
        this.filesSize = filesSize;
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Long getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(Long filesSize) {
        this.filesSize = filesSize;
    }
}
