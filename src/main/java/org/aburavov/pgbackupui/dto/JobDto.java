package org.aburavov.pgbackupui.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.aburavov.pgbackupui.models.Job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class JobDto {

    private String id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Connection ID is required")
    private String connectionId;

    @NotBlank(message = "Storage ID is required")
    private String storageId;

    @NotNull(message = "Retention count is required")
    @Min(value = 1, message = "Retention count must be at least 1")
    private Integer retentionCount;

    private String schedule;

    private List<TableConfigDto> tables;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobDto() {
    }

    public static JobDto from(Job job) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setConnectionId(job.getConnectionId());
        dto.setStorageId(job.getStorageId());
        dto.setRetentionCount(job.getRetentionCount());
        dto.setSchedule(job.getSchedule());

        if (job.getTables() != null) {
            dto.setTables(job.getTables().stream()
                    .map(TableConfigDto::from)
                    .collect(Collectors.toList()));
        }

        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }

    public Job toEntity() {
        Job job = new Job();
        job.setId(this.id);
        job.setName(this.name);
        job.setConnectionId(this.connectionId);
        job.setStorageId(this.storageId);
        job.setRetentionCount(this.retentionCount);
        job.setSchedule(this.schedule);

        if (this.tables != null) {
            job.setTables(this.tables.stream()
                    .map(TableConfigDto::toEntity)
                    .collect(Collectors.toList()));
        }

        return job;
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

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public Integer getRetentionCount() {
        return retentionCount;
    }

    public void setRetentionCount(Integer retentionCount) {
        this.retentionCount = retentionCount;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public List<TableConfigDto> getTables() {
        return tables;
    }

    public void setTables(List<TableConfigDto> tables) {
        this.tables = tables;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class TableConfigDto {
        @NotBlank(message = "Table name is required")
        private String tableName;

        private List<String> columns;

        public TableConfigDto() {
        }

        public static TableConfigDto from(Job.TableConfig tableConfig) {
            TableConfigDto dto = new TableConfigDto();
            dto.setTableName(tableConfig.getTableName());
            dto.setColumns(tableConfig.getColumns());
            return dto;
        }

        public Job.TableConfig toEntity() {
            return new Job.TableConfig(this.tableName, this.columns);
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }
    }
}
