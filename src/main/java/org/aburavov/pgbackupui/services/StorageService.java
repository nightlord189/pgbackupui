package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.models.StorageType;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.aburavov.pgbackupui.repositories.StorageRepository;
import org.aburavov.pgbackupui.services.storage.FileStorageService;
import org.aburavov.pgbackupui.services.storage.IStorageService;
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

    private final StorageRepository storageRepository;
    private final JobRepository jobRepository;
    private final Map<StorageType, IStorageService> storageServices;

    public StorageService(StorageRepository storageRepository, JobRepository jobRepository,
                          FileStorageService fileStorageService) {
        this.storageRepository = storageRepository;
        this.jobRepository = jobRepository;
        this.storageServices = new EnumMap<>(StorageType.class);
        this.storageServices.put(StorageType.LOCAL, fileStorageService);
        // S3 implementation will be added here later
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
        if (storageRepository.existsByName(storage.getName())) {
            throw new IllegalArgumentException(
                "Storage with name '" + storage.getName() + "' already exists"
            );
        }

        storage.validateTypeSpecificFields(false);

        return storageRepository.save(storage);
    }

    public Storage update(String id, Storage storage) {
        Storage existing = storageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Storage not found: " + id));

        if (!existing.getName().equals(storage.getName())
            && storageRepository.existsByName(storage.getName())) {
            throw new IllegalArgumentException(
                "Storage with name '" + storage.getName() + "' already exists"
            );
        }

        storage.validateTypeSpecificFields(true);

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

        return storageRepository.save(existing);
    }

    public void delete(String id) {
        if (!storageRepository.existsById(id)) {
            throw new IllegalArgumentException("Storage not found: " + id);
        }
        if (jobRepository.existsByStorageId(id)) {
            throw new IllegalArgumentException("Cannot delete storage: it is used by one or more jobs");
        }
        storageRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return storageRepository.existsByName(name);
    }

    public String writeBackup(Storage storage, List<TableBackupData> backupDataList) throws IOException {
        IStorageService storageService = getStorageService(storage.getType());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupDirName = "backup_" + timestamp;

        String backupPath = storageService.createDirectory(storage.getPath(), backupDirName);

        for (TableBackupData tableData : backupDataList) {
            storageService.writeFile(backupPath, tableData);
        }

        return backupPath;
    }

    public void applyRetentionPolicy(Storage storage, int retentionCount) throws IOException {
        IStorageService storageService = getStorageService(storage.getType());

        // Get all backup directories sorted by modification time (newest first)
        List<String> backupDirs = storageService.getBackupDirectories(storage.getPath());

        // Keep only the most recent N backups, delete the rest
        if (backupDirs.size() > retentionCount) {
            List<String> dirsToDelete = backupDirs.subList(retentionCount, backupDirs.size());
            for (String dirToDelete : dirsToDelete) {
                storageService.deleteDirectory(storage.getPath(), dirToDelete);
            }
        }
    }

    public long calculateDirectorySize(String directoryPath, StorageType storageType) throws IOException {
        IStorageService storageService = getStorageService(storageType);
        return storageService.calculateDirectorySize(directoryPath);
    }
}
