package com.nghiatr.ticket_booking.shared.storage;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageValidatorTest {

    private FileStorageValidator validator;

    @BeforeEach
    void setUp() {
        // Set max file size to 5MB (5242880 bytes)
        validator = new FileStorageValidator(5242880L);
    }

    @Test
    @DisplayName("Valid JPEG image should pass validation")
    void testValidJpegImage() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        assertDoesNotThrow(() -> validator.validateImageFile(file));
    }

    @Test
    @DisplayName("Valid PNG image should pass validation")
    void testValidPngImage() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "banner.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        assertDoesNotThrow(() -> validator.validateImageFile(file));
    }

    @Test
    @DisplayName("Valid WEBP image should pass validation")
    void testValidWebpImage() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "photo.webp",
                "image/webp",
                new byte[]{1, 2, 3, 4}
        );

        assertDoesNotThrow(() -> validator.validateImageFile(file));
    }

    @Test
    @DisplayName("Null or empty file should throw INVALID_FILE")
    void testEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        AppException ex1 = assertThrows(AppException.class, () -> validator.validateImageFile(null));
        assertEquals(FileErrorCode.INVALID_FILE, ex1.getErrorCode());

        AppException ex2 = assertThrows(AppException.class, () -> validator.validateImageFile(emptyFile));
        assertEquals(FileErrorCode.INVALID_FILE, ex2.getErrorCode());
    }

    @Test
    @DisplayName("File exceeding size limit should throw FILE_SIZE_EXCEEDED")
    void testFileSizeExceeded() {
        // Create a 6MB file
        byte[] largeBytes = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "huge.jpg",
                "image/jpeg",
                largeBytes
        );

        AppException ex = assertThrows(AppException.class, () -> validator.validateImageFile(largeFile));
        assertEquals(FileErrorCode.FILE_SIZE_EXCEEDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Unsupported MIME type should throw UNSUPPORTED_FILE_TYPE")
    void testUnsupportedMimeType() {
        MockMultipartFile textFile = new MockMultipartFile(
                "image",
                "notes.txt",
                "text/plain",
                new byte[]{1, 2, 3}
        );

        AppException ex = assertThrows(AppException.class, () -> validator.validateImageFile(textFile));
        assertEquals(FileErrorCode.UNSUPPORTED_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Unsupported file extension should throw UNSUPPORTED_FILE_TYPE")
    void testUnsupportedExtension() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "image",
                "malicious.exe",
                "image/jpeg", // spoofed mime type
                new byte[]{1, 2, 3}
        );

        AppException ex = assertThrows(AppException.class, () -> validator.validateImageFile(exeFile));
        assertEquals(FileErrorCode.UNSUPPORTED_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    @DisplayName("extractExtension should return correct extension")
    void testExtractExtension() {
        assertEquals("png", validator.extractExtension("my-photo.png"));
        assertEquals("jpeg", validator.extractExtension("folder/image.jpeg"));
        assertEquals("jpg", validator.extractExtension("no-extension"));
        assertEquals("jpg", validator.extractExtension(null));
    }
}
