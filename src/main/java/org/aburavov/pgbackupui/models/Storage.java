package org.aburavov.pgbackupui.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Document(collection = "storages")
public class Storage {

    @Id
    private String id;

    @NotBlank(message = "Storage name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Indexed(unique = true)
    private String name;

    @NotNull(message = "Storage type is required")
    private StorageType type;

    private String path;

    private String bucket;
    private String region;
    private String prefix;
    private String accessKey;
    private String secretKey;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Storage() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates storage-specific fields based on storage type
     * @param isUpdate true if this is an update operation (secretKey is optional), false for create
     * @throws IllegalArgumentException if validation fails
     */
    public void validateTypeSpecificFields(boolean isUpdate) {
        if (this.type == StorageType.LOCAL) {
            if (this.path == null || this.path.isBlank()) {
                throw new IllegalArgumentException("Path is required for LOCAL storage type");
            }
        } else if (this.type == StorageType.S3) {
            if (this.bucket == null || this.bucket.isBlank()) {
                throw new IllegalArgumentException("Bucket is required for S3 storage type");
            }
            if (this.region == null || this.region.isBlank()) {
                throw new IllegalArgumentException("Region is required for S3 storage type");
            }
            if (this.accessKey == null || this.accessKey.isBlank()) {
                throw new IllegalArgumentException("Access key is required for S3 storage type");
            }
            if (!isUpdate && (this.secretKey == null || this.secretKey.isBlank())) {
                throw new IllegalArgumentException("Secret key is required for S3 storage type");
            }
        }
    }
}
