package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class LocalStorageServiceTest {

    // OSの一時ディレクトリを使用するのでテスト後に自動クリーンアップされる
    @TempDir
    Path tempDir;

    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        // Springコンテキストを立てずに注入
        ReflectionTestUtils.setField(localStorageService, "storagePath", tempDir.toString());
    }

    @Test
    void store_ファイルを保存しkeyを返す() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "hello".getBytes());
        String key = "attachments/abc_test.pdf";

        String returned = localStorageService.store(file, key);

        assertThat(returned).isEqualTo(key);
        Path saved = tempDir.resolve(key);
        assertThat(Files.exists(saved)).isTrue();
        assertThat(Files.readString(saved)).isEqualTo("hello");
    }

    @Test
    void store_親ディレクトリが無い場合でも作成して保存する() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        // attachments サブディレクトリはまだ存在しない
        String key = "attachments/xyz_test.pdf";

        localStorageService.store(file, key);

        assertThat(Files.exists(tempDir.resolve("attachments"))).isTrue();
        assertThat(Files.exists(tempDir.resolve(key))).isTrue();
    }

    @Test
    void getDownloadUrl_fileのURIを返す() {
        String storedPath = "attachments/abc_test.pdf";

        String url = localStorageService.getDownloadUrl(storedPath);

        assertThat(url).startsWith("file:");
        // file URI として解釈でき、保存先の絶対パスに解決される（OS差に依存しない検証）
        assertThat(Path.of(URI.create(url)))
                .isEqualTo(tempDir.resolve(storedPath).toAbsolutePath().normalize());
    }

    @Test
    void delete_保存済みファイルを削除する() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "bye".getBytes());
        String key = "attachments/del_test.pdf";
        localStorageService.store(file, key);
        assertThat(Files.exists(tempDir.resolve(key))).isTrue();

        localStorageService.delete(key);

        assertThat(Files.exists(tempDir.resolve(key))).isFalse();
    }

    @Test
    void delete_存在しないファイルでも例外を投げない() {
        // Files.deleteIfExists のため、対象が無くても失敗しない
        localStorageService.delete("attachments/not_exists.pdf");
    }

    @Test
    void getUrlExpiration_期限なしのためMAXを返す() {
        assertThat(localStorageService.getUrlExpiration()).isEqualTo(LocalDateTime.MAX);
    }
}
