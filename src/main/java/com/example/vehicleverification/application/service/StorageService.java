package com.example.vehicleverification.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String key);

    String getDownloadUrl(String storedPath);

    void delete(String storedPath);
}
