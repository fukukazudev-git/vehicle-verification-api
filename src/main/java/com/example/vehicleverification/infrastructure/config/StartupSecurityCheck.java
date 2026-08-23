package com.example.vehicleverification.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupSecurityCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSecurityCheck.class);

    private final JwtProperties jwtProperties;
    private final String adminPassword;

    public StartupSecurityCheck(JwtProperties jwtProperties, @Value("${ADMIN_PASSWORD:admin}") String adminPassword) {
        this.jwtProperties = jwtProperties;
        this.adminPassword = adminPassword;
    }

    // 解決済みの値と組み込みデフォルト値が一致するかで警告ログを出す(提供経路に依存しない形)
    @Override
    public void run(ApplicationArguments args) {

        if ("local-dev-secret-key-must-be-at-least-32-characters".equals(jwtProperties.getSecret())) {
            log.warn("JWT_SECRETが開発用デフォルトのままです。本番では必ず上書きしてください。");
        }

        if ("admin".equals(adminPassword)) {
            log.warn("ADMIN_PASSWORDが開発用デフォルトのままです。本番では必ず上書きしてください。");
        }
    }
}
