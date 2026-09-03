package com.example.vehicleverification.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("local")
public class LocalStorageService implements StorageService {
    @Override
    public String store(MultipartFile file, String key) {
        // Implement the logic to store the file in local storage
        return null;
    }

    @Override
    public String getDownloadUrl(String storedPath) {
        // Implement the logic to generate a download URL for the stored file
        return null;
    }

    @Override
    public void delete(String storedPath) {
        // Implement the logic to delete the file from local storage
    }
}
