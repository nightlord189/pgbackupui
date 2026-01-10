package org.aburavov.pgbackupui.services.storage;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementation of IStorageService for local filesystem storage
 */
@Service
public class FileStorageService implements IStorageService {

    @Override
    public String createDirectory(String storagePath, String directoryName) throws IOException {
        Path backupPath = Paths.get(storagePath, directoryName);
        Files.createDirectories(backupPath);
        return backupPath.toString();
    }

    @Override
    public void writeFile(String backupPath, TableBackupData tableData) throws IOException {
        String tableName = tableData.getTableName();
        Path sqlFilePath = Paths.get(backupPath, tableName + ".sql");

        try (BufferedWriter writer = Files.newBufferedWriter(sqlFilePath)) {
            for (String sqlStatement : tableData.getSqlStatements()) {
                writer.write(sqlStatement);
                writer.newLine();
            }
        }
    }

    @Override
    public List<String> getBackupDirectories(String storagePath) throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("backup_"))
                    .sorted((p1, p2) -> {
                        // Sort by last modified time (newest first)
                        try {
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    @Override
    public void deleteDirectory(String storagePath, String directoryName) throws IOException {
        Path directory = Paths.get(storagePath, directoryName);
        if (Files.exists(directory)) {
            try (Stream<Path> walk = Files.walk(directory)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Log error but continue
                                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            }
                        });
            }
        }
    }

    @Override
    public long calculateDirectorySize(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        }
    }
}
