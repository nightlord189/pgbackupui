package org.aburavov.pgbackupui.services.storage;

import org.aburavov.pgbackupui.dto.TableBackupData;
import org.aburavov.pgbackupui.models.Storage;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class S3StorageService implements IStorageService {
    /**
     * Creates an S3 client with credentials from Storage configuration
     */
    private S3Client createS3Client(Storage storage) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                storage.getAccessKey(),
                storage.getSecretKey()
        );

        return S3Client.builder()
                .region(Region.of(storage.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * Builds full S3 key from storage prefix, base path, and object name
     */
    private String buildS3Key(Storage storage, String... pathParts) {
        List<String> parts = new ArrayList<>();

        if (storage.getPrefix() != null && !storage.getPrefix().isBlank()) {
            parts.add(storage.getPrefix());
        }

        parts.addAll(Arrays.asList(pathParts));

        return String.join("/", parts).replaceAll("/+", "/");
    }

    /**
     * For S3, directories are just prefixes; no actual creation needed
     * @param storage Storage configuration
     * @param directoryName name of the directory to create
     * @return directory name that will be used as prefix for objects
     */
    @Override
    public String createDirectory(Storage storage, String directoryName) {
        String prefix = storage.getPrefix() != null && !storage.getPrefix().isBlank()
                ? storage.getPrefix() + "/" + directoryName
                : directoryName;
        return prefix;
    }

    @Override
    public void writeFile(Storage storage, String backupPath, TableBackupData tableData) throws IOException {
        String tableName = tableData.getTableName();
        String fileName = tableName + ".sql";

        String s3Key = backupPath + "/" + fileName;


        StringBuilder content = new StringBuilder();
        for (String sqlStatement : tableData.getSqlStatements()) {
            content.append(sqlStatement).append("\n");
        }

        try (S3Client s3Client = createS3Client(storage)) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(storage.getBucket())
                    .key(s3Key)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(content.toString()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getBackupDirectories(Storage storage) throws IOException {
        String prefix = storage.getPrefix() != null && !storage.getPrefix().isBlank()
                ? storage.getPrefix() + "/"
                : "";

        try (S3Client s3Client = createS3Client(storage)) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(storage.getBucket())
                    .prefix(prefix)
                    .delimiter("/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);


            return listResponse.commonPrefixes().stream()
                    .map(CommonPrefix::prefix)
                    .map(p -> p.substring(prefix.length())) // Remove base prefix
                    .map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p) // Remove trailing slash
                    .filter(p -> p.startsWith("backup_"))
                    .sorted((p1, p2) -> {
                        return p2.compareTo(p1);
                    })
                    .collect(Collectors.toList());
        } catch (S3Exception e) {
            throw new IOException("Failed to list S3 objects: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDirectory(Storage storage, String directoryName) throws IOException {
        String prefix = buildS3Key(storage, directoryName) + "/";

        try (S3Client s3Client = createS3Client(storage)) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(storage.getBucket())
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            if (listResponse.contents().isEmpty()) {
                return;
            }

            List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .collect(Collectors.toList());

            Delete delete = Delete.builder()
                    .objects(objectsToDelete)
                    .build();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(storage.getBucket())
                    .delete(delete)
                    .build();

            s3Client.deleteObjects(deleteRequest);
        } catch (S3Exception e) {
            throw new IOException("Failed to delete S3 objects: " + e.getMessage(), e);
        }
    }

    @Override
    public long calculateDirectorySize(Storage storage, String directoryPath) throws IOException {
        String prefix = directoryPath + "/";

        try (S3Client s3Client = createS3Client(storage)) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(storage.getBucket())
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            return listResponse.contents().stream()
                    .mapToLong(S3Object::size)
                    .sum();
        } catch (S3Exception e) {
            throw new IOException("Failed to calculate S3 directory size: " + e.getMessage(), e);
        }
    }
}
