package org.aburavov.pgbackupui.services.storage;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Storage;

import java.io.IOException;
import java.util.List;

/**
 * Interface for different storage implementations (Local filesystem, S3, etc.)
 */
public interface IStorageService {

    /**
     * Creates a backup directory with the given name in the storage
     * @param storage Storage configuration
     * @param directoryName name of the directory to create
     * @return full path to the created directory
     */
    String createDirectory(Storage storage, String directoryName) throws IOException;

    /**
     * Writes a table backup data to a file
     * @param storage Storage configuration
     * @param backupPath path to the backup directory
     * @param tableData table data to write
     */
    void writeFile(Storage storage, String backupPath, TableBackupData tableData) throws IOException;

    /**
     * Gets list of backup directories from storage
     * @param storage Storage configuration
     * @return list of backup directory names sorted by modification time (newest first)
     */
    List<String> getBackupDirectories(Storage storage) throws IOException;

    /**
     * Deletes a directory with all its contents
     * @param storage Storage configuration
     * @param directoryName name of the directory to delete
     */
    void deleteDirectory(Storage storage, String directoryName) throws IOException;

    /**
     * Calculates total size of files in a directory
     * @param storage Storage configuration
     * @param directoryPath full path to the directory
     * @return total size in bytes
     */
    long calculateDirectorySize(Storage storage, String directoryPath) throws IOException;
}
