package com.example.vehicleverification.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.vehicleverification.domain.repository.UserRepository;

import com.example.vehicleverification.domain.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class InitialAdminRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(InitialAdminRunner.class);

    // 各設定値(環境変数があればそれを優先、なければデフォルト値)
    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    @Value("${ADMIN_DISPLAY_NAME:Administrator}")
    private String adminDisplayName;

    @Value("${ADMIN_DEPARTMENT:Administration}")
    private String adminDepartment;

    public InitialAdminRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void run(String... args) throws Exception {

        // ユーザが存在しない場合に初期管理者ユーザを作成する
        if (userRepository.count() == 0) {

            User admin = new User(
                    adminUsername,
                    passwordEncoder.encode(adminPassword), // ハッシュ化
                    adminDisplayName,
                    "ADMIN",
                    adminDepartment);

            userRepository.save(admin);

            log.info("初期管理者を作成しました: {}", adminUsername);

        } else {

            return;

        }

    }

}
