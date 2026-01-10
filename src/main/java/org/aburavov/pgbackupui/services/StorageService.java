package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.models.StorageType;
import org.aburavov.pgbackupui.repositories.StorageRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class StorageService {

    private final StorageRepository storageRepository;

    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
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

        validateStorageFields(storage, false);

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

        validateStorageFields(storage, true);

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
        storageRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return storageRepository.existsByName(name);
    }

    private void validateStorageFields(Storage storage, boolean isUpdate) {
        if (storage.getType() == StorageType.LOCAL) {
            if (storage.getPath() == null || storage.getPath().isBlank()) {
                throw new IllegalArgumentException("Path is required for LOCAL storage type");
            }
        } else if (storage.getType() == StorageType.S3) {
            if (storage.getBucket() == null || storage.getBucket().isBlank()) {
                throw new IllegalArgumentException("Bucket is required for S3 storage type");
            }
            if (storage.getRegion() == null || storage.getRegion().isBlank()) {
                throw new IllegalArgumentException("Region is required for S3 storage type");
            }
            if (storage.getAccessKey() == null || storage.getAccessKey().isBlank()) {
                throw new IllegalArgumentException("Access key is required for S3 storage type");
            }
            if (!isUpdate && (storage.getSecretKey() == null || storage.getSecretKey().isBlank())) {
                throw new IllegalArgumentException("Secret key is required for S3 storage type");
            }
        }
    }

    public String writeBackup(Storage storage, List<TableBackupData> backupDataList) throws IOException {
        if (storage.getType() != StorageType.LOCAL) {
            throw new UnsupportedOperationException("Only LOCAL storage type is supported currently");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupDirName = "backup_" + timestamp;
        Path backupPath = Paths.get(storage.getPath(), backupDirName);

        Files.createDirectories(backupPath);

        for (TableBackupData tableData : backupDataList) {
            writeTableToFile(backupPath, tableData);
        }

        return backupPath.toString();
    }

    private void writeTableToFile(Path backupPath, TableBackupData tableData) throws IOException {
        String tableName = tableData.getTableName();
        Path sqlFilePath = backupPath.resolve(tableName + ".sql");

        try (BufferedWriter writer = Files.newBufferedWriter(sqlFilePath)) {
            for (String sqlStatement : tableData.getSqlStatements()) {
                writer.write(sqlStatement);
                writer.newLine();
            }
        }
    }
}
