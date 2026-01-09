package org.aburavov.pgbackupui.dto;

import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.models.StorageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class StorageDto {

    private String id;

    @NotBlank(message = "Storage name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Storage type is required")
    private StorageType type;

    // For LOCAL storage
    private String path;

    // For S3 storage
    private String bucket;
    private String region;
    private String prefix;
    private String accessKey;
    private String secretKey;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StorageDto from(Storage storage) {
        StorageDto dto = new StorageDto();
        dto.setId(storage.getId());
        dto.setName(storage.getName());
        dto.setType(storage.getType());
        dto.setPath(storage.getPath());
        dto.setBucket(storage.getBucket());
        dto.setRegion(storage.getRegion());
        dto.setPrefix(storage.getPrefix());
        dto.setAccessKey(storage.getAccessKey());
        dto.setSecretKey(storage.getSecretKey());
        dto.setCreatedAt(storage.getCreatedAt());
        dto.setUpdatedAt(storage.getUpdatedAt());
        return dto;
    }

    public Storage toEntity() {
        Storage storage = new Storage();
        storage.setId(this.id);
        storage.setName(this.name);
        storage.setType(this.type);
        storage.setPath(this.path);
        storage.setBucket(this.bucket);
        storage.setRegion(this.region);
        storage.setPrefix(this.prefix);
        storage.setAccessKey(this.accessKey);
        storage.setSecretKey(this.secretKey);
        if (this.createdAt != null) {
            storage.setCreatedAt(this.createdAt);
        }
        if (this.updatedAt != null) {
            storage.setUpdatedAt(this.updatedAt);
        }
        return storage;
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

    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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
}
