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

    @NotNull(message = "Status is required")
    private JobRunStatus status;

    private String errorMessage;

    private JobRunTriggerType triggeredBy;

    private String folderName;

    private Long filesSize;

    public JobRun() {
        this.startedAt = LocalDateTime.now();
        this.status = JobRunStatus.RUNNING;
    }

    public JobRun(String jobId, JobRunTriggerType triggeredBy) {
        this();
        this.jobId = jobId;
        this.triggeredBy = triggeredBy;
    }

    public void markSuccess(String folderName, Long filesSize) {
        this.status = JobRunStatus.SUCCESS;
        this.finishedAt = LocalDateTime.now();
        this.folderName = folderName;
        this.filesSize = filesSize;
    }

    public void markFailed(String errorMessage) {
        this.status = JobRunStatus.FAILED;
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

    public JobRunStatus getStatus() {
        return status;
    }

    public void setStatus(JobRunStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JobRunTriggerType getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(JobRunTriggerType triggeredBy) {
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
