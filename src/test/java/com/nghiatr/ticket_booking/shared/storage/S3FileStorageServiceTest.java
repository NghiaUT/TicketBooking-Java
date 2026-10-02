package com.nghiatr.ticket_booking.shared.storage;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private FileStorageValidator fileStorageValidator;

    @InjectMocks
    private S3FileStorageService s3FileStorageService;

    private final String bucketName = "test-bucket";
    private final String region = "ap-southeast-1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3FileStorageService, "bucketName", bucketName);
        ReflectionTestUtils.setField(s3FileStorageService, "region", region);
        ReflectionTestUtils.setField(s3FileStorageService, "endpoint", "");
    }

    @Test
    @DisplayName("uploadFile should validate file, call S3 putObject, and return valid S3 URL")
    void testUploadFileSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "event-cover.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        when(fileStorageValidator.extractExtension("event-cover.png")).thenReturn("png");

        String url = s3FileStorageService.uploadFile(file, "events");

        verify(fileStorageValidator).validateImageFile(file);
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = putCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertTrue(capturedRequest.key().startsWith("events/"));
        assertTrue(capturedRequest.key().endsWith(".png"));
        assertEquals("image/png", capturedRequest.contentType());
        assertEquals(4L, capturedRequest.contentLength());

        assertNotNull(url);
        assertTrue(url.startsWith("https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/"));
        assertTrue(url.endsWith(".png"));
    }

    @Test
    @DisplayName("uploadFile with endpoint override should generate endpoint-based URL")
    void testUploadFileWithEndpointOverride() {
        ReflectionTestUtils.setField(s3FileStorageService, "endpoint", "http://localhost:4566");

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2}
        );

        when(fileStorageValidator.extractExtension("cover.jpg")).thenReturn("jpg");

        String url = s3FileStorageService.uploadFile(file, "events");

        assertTrue(url.startsWith("http://localhost:4566/test-bucket/events/"));
        assertTrue(url.endsWith(".jpg"));
    }

    @Test
    @DisplayName("uploadFile when S3 throws S3Exception should throw AppException(FILE_UPLOAD_FAILED)")
    void testUploadFileS3Exception() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2}
        );

        when(fileStorageValidator.extractExtension("cover.jpg")).thenReturn("jpg");

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("Access Denied")
                .errorCode("403")
                .build();
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(errorDetails)
                .message("Access Denied")
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(s3Exception);

        AppException ex = assertThrows(AppException.class, () -> s3FileStorageService.uploadFile(file, "events"));
        assertEquals(FileErrorCode.FILE_UPLOAD_FAILED, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Access Denied"));
    }

    @Test
    @DisplayName("deleteFile should extract key and call S3 deleteObject")
    void testDeleteFile() {
        String fileUrl = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/test-uuid.jpg";

        s3FileStorageService.deleteFile(fileUrl);

        ArgumentCaptor<DeleteObjectRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteCaptor.capture());

        DeleteObjectRequest request = deleteCaptor.getValue();
        assertEquals(bucketName, request.bucket());
        assertEquals("events/test-uuid.jpg", request.key());
    }

    @Test
    @DisplayName("deleteFile with blank URL should do nothing")
    void testDeleteFileBlank() {
        s3FileStorageService.deleteFile(null);
        s3FileStorageService.deleteFile("   ");

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("extractKey should handle both virtual-hosted-style and path-style URLs")
    void testExtractKey() {
        assertEquals("events/abc.png",
                s3FileStorageService.extractKey("https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/abc.png"));

        assertEquals("events/abc.png",
                s3FileStorageService.extractKey("http://localhost:4566/test-bucket/events/abc.png"));

        assertEquals("events/abc.png",
                s3FileStorageService.extractKey("events/abc.png"));
    }
}
