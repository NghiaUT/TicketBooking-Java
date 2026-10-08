package com.nghiatr.ticket_booking.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.region:ap-southeast-1}")
    private String region;

    @Value("${aws.s3.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    /**
     * Khởi tạo Bean S3Client kết nối tới dịch vụ lưu trữ AWS S3 hoặc tương thích (LocalStack/MinIO).
     *
     * @return đối tượng S3Client đã được cấu hình vùng và chứng thực
     */
    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId.trim(), secretAccessKey.trim())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint.trim()))
                    .forcePathStyle(true);
        }

        return builder.build();
    }
}
