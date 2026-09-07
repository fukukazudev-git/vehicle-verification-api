package com.example.vehicleverification.application.service;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String key);

    String getDownloadUrl(String storedPath);

    void delete(String storedPath);

    LocalDateTime getUrlExpiration();
}
