package com.example.vehicleverification.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("prod")
public class S3StorageService implements StorageService {
    @Override
    public String store(MultipartFile file, String key) {
        // S3にファイルをアップロードする処理を実装
        return null;
    }

    @Override
    public String getDownloadUrl(String storedPath) {
        // S3からダウンロードURLを取得する処理を実装
        return null;
    }

    @Override
    public void delete(String storedPath) {
        // S3からファイルを削除する処理を実装
    }
}
