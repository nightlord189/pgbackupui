package org.aburavov.pgbackupui.services.storage;

import org.aburavov.pgbackupui.dto.TableBackupData;

import java.io.IOException;
import java.util.List;

/**
 * Interface for different storage implementations (Local filesystem, S3, etc.)
 */
public interface IStorageService {

    /**
     * Creates a backup directory with the given name in the storage path
     * @param storagePath base storage path
     * @param directoryName name of the directory to create
     * @return full path to the created directory
     */
    String createDirectory(String storagePath, String directoryName) throws IOException;

    /**
     * Writes a table backup data to a file
     * @param backupPath path to the backup directory
     * @param tableData table data to write
     */
    void writeFile(String backupPath, TableBackupData tableData) throws IOException;

    /**
     * Gets list of backup directories from storage path
     * @param storagePath base storage path
     * @return list of backup directory names sorted by modification time (newest first)
     */
    List<String> getBackupDirectories(String storagePath) throws IOException;

    /**
     * Deletes a directory with all its contents
     * @param storagePath base storage path
     * @param directoryName name of the directory to delete
     */
    void deleteDirectory(String storagePath, String directoryName) throws IOException;

    /**
     * Calculates total size of files in a directory
     * @param directoryPath full path to the directory
     * @return total size in bytes
     */
    long calculateDirectorySize(String directoryPath) throws IOException;
}
