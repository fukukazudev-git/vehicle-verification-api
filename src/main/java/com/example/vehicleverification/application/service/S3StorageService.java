package com.example.vehicleverification.application.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Profile("prod")
public class S3StorageService implements StorageService {
    @Value("${storage.s3.bucket}")
    private String bucket;

    @Value("${storage.s3.region:us-east-1}")
    private String region;

    private S3Client s3Client;
    private S3Presigner presigner;

    // フィールド注入後に初期化する
    @PostConstruct
    public void init() {
        Region r = Region.of(region);
        this.s3Client = S3Client.builder().region(r).build();
        this.presigner = S3Presigner.builder().region(r).build();
    }

    @Override
    public String store(MultipartFile file, String key) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    // 中身のバイト列
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            throw new RuntimeException("S3へのアップロードに失敗しました", e);
        }
    }

    @Override
    public String getDownloadUrl(String storedPath) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder().bucket(bucket).key(storedPath).build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String storedPath) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(storedPath).build());
    }

    @Override
    public LocalDateTime getUrlExpiration() {
        return LocalDateTime.now().plusHours(1);
    }
}
