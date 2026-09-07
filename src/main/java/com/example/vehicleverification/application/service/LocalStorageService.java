package com.example.vehicleverification.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("local")
public class LocalStorageService implements StorageService {

    @Value("${storage.local.path:./uploads}")
    private String storagePath;

    @Override
    public String store(MultipartFile file, String key) {
        try {
            Path target = Path.of(storagePath, key);
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("ファイルの保存に失敗しました", e);
        }
    }

    @Override
    public String getDownloadUrl(String storedPath) {
        return Path.of(storagePath, storedPath)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
    }

    @Override
    public void delete(String storedPath) {
        try {
            Files.deleteIfExists(Path.of(storagePath, storedPath));
        } catch (IOException e) {
            throw new RuntimeException("ファイルの削除に失敗しました", e);
        }
    }

    @Override
    public LocalDateTime getUrlExpiration() {
        return LocalDateTime.MAX;
    }
}
