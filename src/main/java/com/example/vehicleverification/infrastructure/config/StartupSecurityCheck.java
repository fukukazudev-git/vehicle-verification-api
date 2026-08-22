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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    // 解決済みの値を注入し、組み込みデフォルト値と一致するかで警告ログを出す(提供経路に依存しない形)
    @Override
    public void run(ApplicationArguments args) {

        // レベルはwarnとし、起動ログで黄色くハイライトさせる
        if ("local-dev-secret-key-must-be-at-least-32-characters".equals(jwtSecret)) {
            log.warn("JWT_SECRETが開発用デフォルトのままです。本番では必ず上書きしてください。");
        }

        if ("admin".equals(adminPassword)) {
            log.warn("ADMIN_PASSWORDが開発用デフォルトのままです。本番では必ず上書きしてください。");
        }
    }
}
