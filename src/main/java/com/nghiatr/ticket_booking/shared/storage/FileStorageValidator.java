package com.nghiatr.ticket_booking.shared.storage;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class FileStorageValidator {

    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg",
            "jpeg",
            "png",
            "webp"
    );

    private final long maxFileSizeBytes;

    public FileStorageValidator(
            @Value("${app.storage.max-file-size-bytes:5242880}") long maxFileSizeBytes
    ) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(FileErrorCode.INVALID_FILE);
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new AppException(FileErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            String extension = extractExtension(originalFilename);
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new AppException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
            }
        }
    }

    public String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
