package com.example.vehicleverification.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// storage.* の設定を型安全に集約する
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {

    private Local local = new Local();
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Local {
        private String path = "./uploads";
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String region = "ap-northeast-1";
    }
}
