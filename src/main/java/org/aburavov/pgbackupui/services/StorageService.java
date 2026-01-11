package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.models.StorageType;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.aburavov.pgbackupui.repositories.StorageRepository;
import org.aburavov.pgbackupui.services.storage.FileStorageService;
import org.aburavov.pgbackupui.services.storage.IStorageService;
import org.aburavov.pgbackupui.services.storage.S3StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    private final StorageRepository storageRepository;
    private final JobRepository jobRepository;
    private final Map<StorageType, IStorageService> storageServices;

    public StorageService(StorageRepository storageRepository, JobRepository jobRepository,
                          FileStorageService fileStorageService, S3StorageService s3StorageService) {
        this.storageRepository = storageRepository;
        this.jobRepository = jobRepository;
        this.storageServices = new EnumMap<>(StorageType.class);
        this.storageServices.put(StorageType.LOCAL, fileStorageService);
        this.storageServices.put(StorageType.S3, s3StorageService);
    }

    private IStorageService getStorageService(StorageType type) {
        IStorageService service = storageServices.get(type);
        if (service == null) {
            throw new UnsupportedOperationException("Storage type " + type + " is not supported yet");
        }
        return service;
    }

    public List<Storage> findAll() {
        return storageRepository.findAll();
    }

    public Optional<Storage> findById(String id) {
        return storageRepository.findById(id);
    }

    public Optional<Storage> findByName(String name) {
        return storageRepository.findByName(name);
    }

    public Storage create(Storage storage) {
        logger.info("Creating storage: {} (type={})", storage.getName(), storage.getType());
        if (storageRepository.existsByName(storage.getName())) {
            logger.error("Storage with name '{}' already exists", storage.getName());
            throw new IllegalArgumentException(
                "Storage with name '" + storage.getName() + "' already exists"
            );
        }

        storage.validate(false);

        Storage saved = storageRepository.save(storage);
        logger.info("Storage created successfully: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    public Storage update(String id, Storage storage) {
        logger.info("Updating storage: {} (id={})", storage.getName(), id);
        Storage existing = storageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Storage not found: " + id));

        if (!existing.getName().equals(storage.getName())
            && storageRepository.existsByName(storage.getName())) {
            logger.error("Storage with name '{}' already exists", storage.getName());
            throw new IllegalArgumentException(
                "Storage with name '" + storage.getName() + "' already exists"
            );
        }

        storage.validate(true);

        existing.setName(storage.getName());
        existing.setType(storage.getType());
        existing.setPath(storage.getPath());
        existing.setBucket(storage.getBucket());
        existing.setRegion(storage.getRegion());
        existing.setPrefix(storage.getPrefix());
        existing.setAccessKey(storage.getAccessKey());

        if (storage.getSecretKey() != null && !storage.getSecretKey().isBlank()) {
            existing.setSecretKey(storage.getSecretKey());
        }

        existing.updateTimestamp();

        Storage updated = storageRepository.save(existing);
        logger.info("Storage updated successfully: {}", updated.getName());
        return updated;
    }

    public void delete(String id) {
        logger.info("Deleting storage: {}", id);
        if (!storageRepository.existsById(id)) {
            logger.error("Storage not found: {}", id);
            throw new IllegalArgumentException("Storage not found: " + id);
        }
        if (jobRepository.existsByStorageId(id)) {
            logger.error("Cannot delete storage {}: it is used by one or more jobs", id);
            throw new IllegalArgumentException("Cannot delete storage: it is used by one or more jobs");
        }
        storageRepository.deleteById(id);
        logger.info("Storage deleted successfully: {}", id);
    }

    public boolean existsByName(String name) {
        return storageRepository.existsByName(name);
    }

    public String writeBackup(Storage storage, List<TableBackupData> backupDataList) throws IOException {
        logger.info("Writing backup to storage: {} (type={})", storage.getName(), storage.getType());
        IStorageService storageService = getStorageService(storage.getType());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupDirName = "backup_" + timestamp;

        String backupPath = storageService.createDirectory(storage, backupDirName);
        logger.debug("Created backup directory: {}", backupPath);

        for (TableBackupData tableData : backupDataList) {
            logger.debug("Writing table: {}", tableData.getTableName());
            storageService.writeFile(storage, backupPath, tableData);
        }

        logger.info("Backup written successfully: {} tables to {}", backupDataList.size(), backupPath);
        return backupPath;
    }

    public void applyRetentionPolicy(Storage storage, int retentionCount) throws IOException {
        logger.info("Applying retention policy: keeping {} most recent backups for storage {}",
            retentionCount, storage.getName());
        IStorageService storageService = getStorageService(storage.getType());

        List<String> backupDirs = storageService.getBackupDirectories(storage);
        logger.debug("Found {} existing backups", backupDirs.size());

        if (backupDirs.size() > retentionCount) {
            List<String> dirsToDelete = backupDirs.subList(retentionCount, backupDirs.size());
            logger.info("Deleting {} old backups", dirsToDelete.size());
            for (String dirToDelete : dirsToDelete) {
                logger.debug("Deleting old backup: {}", dirToDelete);
                storageService.deleteDirectory(storage, dirToDelete);
            }
            logger.info("Retention policy applied successfully");
        } else {
            logger.debug("No backups to delete (current: {}, retention: {})", backupDirs.size(), retentionCount);
        }
    }

    public long calculateDirectorySize(Storage storage, String directoryPath) throws IOException {
        IStorageService storageService = getStorageService(storage.getType());
        return storageService.calculateDirectorySize(storage, directoryPath);
    }
}
