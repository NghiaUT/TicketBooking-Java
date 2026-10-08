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

    /**
     * Khởi tạo bộ kiểm tra tệp tin với giới hạn kích thước tối đa.
     *
     * @param maxFileSizeBytes kích thước tối đa cho phép tính theo bytes
     */
    public FileStorageValidator(
            @Value("${app.storage.max-file-size-bytes:5242880}") long maxFileSizeBytes
    ) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    /**
     * Kiểm tra tính hợp lệ của tệp hình ảnh tải lên (không rỗng, dung lượng và định dạng cho phép).
     *
     * @param file tệp tin tải lên từ client
     * @throws AppException nếu tệp rỗng, vượt quá kích thước hoặc định dạng không được hỗ trợ
     */
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

    /**
     * Trích xuất phần mở rộng (extension) từ tên tệp tin.
     *
     * @param filename tên tệp tin gốc
     * @return chuỗi phần mở rộng của tệp (mặc định "jpg" nếu không xác định được)
     */
    public String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
