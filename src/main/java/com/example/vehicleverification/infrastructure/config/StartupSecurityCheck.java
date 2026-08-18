package com.example.vehicleverification.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupSecurityCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSecurityCheck.class);

    // getenvで環境変数を取得し、nullの場合は警告ログを出力する
    @Override
    public void run(ApplicationArguments args) throws Exception {

        // レベルはwarnとし、起動ログで黄色くハイライトさせる
        if (System.getenv("JWT_SECRET") == null) {
            log.warn("JWT_SECRETが未設定です。開発用デフォルトで起動中。本番では必ず環境変数で設定してください。");
        }

        if (System.getenv("ADMIN_PASSWORD") == null) {
            log.warn("ADMIN_PASSWORD が未設定です。開発用デフォルトで起動中。本番では必ず環境変数で設定してください。");
        }
    }
}
