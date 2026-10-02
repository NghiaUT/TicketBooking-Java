package com.nghiatr.ticket_booking.shared.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Upload file lên dịch vụ lưu trữ (ví dụ S3) vào thư mục chỉ định.
     *
     * @param file   file cần upload
     * @param folder tên thư mục/tiền tố (ví dụ "events")
     * @return URL công khai của file sau khi upload thành công
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Xóa file khỏi dịch vụ lưu trữ dựa vào URL hoặc object key.
     *
     * @param fileUrlOrKey URL hoặc object key cần xóa
     */
    void deleteFile(String fileUrlOrKey);
}
