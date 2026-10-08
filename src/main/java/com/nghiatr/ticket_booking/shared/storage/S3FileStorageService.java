package com.nghiatr.ticket_booking.shared.storage;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final FileStorageValidator fileStorageValidator;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region:ap-southeast-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    @Value("${app.env}")
    private String env;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        fileStorageValidator.validateImageFile(file);

        String extension = fileStorageValidator.extractExtension(file.getOriginalFilename());
        String uploadEnv =  "production".equalsIgnoreCase(env)
                ? "production/"
                : "development/";
        String folderPrefix = StringUtils.hasText(folder) ? folder.trim().replaceAll("^/+|/+$", "") + "/" : "";
        String key = uploadEnv + folderPrefix + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("Successfully uploaded file to S3. Bucket: {}, Key: {}", bucketName, key);
            return buildFileUrl(key);
        } catch (S3Exception e) {
            log.error("AWS S3 error while uploading file with key {}: {}", key, e.awsErrorDetails().errorMessage(), e);
            throw new AppException(FileErrorCode.FILE_UPLOAD_FAILED, "Lỗi từ Amazon S3 khi tải ảnh: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("IO error while reading file stream for key {}: {}", key, e.getMessage(), e);
            throw new AppException(FileErrorCode.FILE_UPLOAD_FAILED, "Lỗi đọc dữ liệu file tải lên");
        } catch (Exception e) {
            log.error("Unexpected error while uploading file with key {}: {}", key, e.getMessage(), e);
            throw new AppException(FileErrorCode.FILE_UPLOAD_FAILED, "Upload ảnh lên S3 thất bại");
        }
    }

    @Override
    public void deleteFile(String fileUrlOrKey) {
        if (!StringUtils.hasText(fileUrlOrKey)) {
            return;
        }

        String key = extractKey(fileUrlOrKey);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3. Bucket: {}, Key: {}", bucketName, key);
        } catch (S3Exception e) {
            log.warn("Failed to delete file from S3. Bucket: {}, Key: {}. Reason: {}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.warn("Unexpected error when deleting file from S3. Bucket: {}, Key: {}. Reason: {}",
                    bucketName, key, e.getMessage());
        }
    }

    public String extractKey(String fileUrlOrKey) {
        if (!fileUrlOrKey.startsWith("http://") && !fileUrlOrKey.startsWith("https://")) {
            return fileUrlOrKey;
        }

        try {
            URI uri = URI.create(fileUrlOrKey);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Nếu URL theo kiểu path-style (ví dụ http://localhost:4566/{bucketName}/events/...)
            if (path.startsWith(bucketName + "/")) {
                path = path.substring(bucketName.length() + 1);
            }

            return path;
        } catch (Exception e) {
            log.warn("Could not parse URI from URL: {}. Using original string as key.", fileUrlOrKey);
            return fileUrlOrKey;
        }
    }

    private String buildFileUrl(String key) {
        if (StringUtils.hasText(endpoint)) {
            String trimmedEndpoint = endpoint.trim().replaceAll("/+$", "");
            return trimmedEndpoint + "/" + bucketName + "/" + key;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
