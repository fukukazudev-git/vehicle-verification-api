package com.example.vehicleverification.application.service;

import com.example.vehicleverification.domain.exception.LoginLockedException;
import com.example.vehicleverification.infrastructure.config.LoginAttemptProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final Clock clock;
    private final LoginAttemptProperties loginAttemptProperties;

    private record Attempt(int failures, Instant lockedUntil) {}

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptServiceImpl(Clock clock, LoginAttemptProperties loginAttemptProperties) {
        this.clock = clock;
        this.loginAttemptProperties = loginAttemptProperties;
    }

    @Override
    public void checkLocked(String username) {
        // init.sqlのusernameには照合順序の指定がないためMySQL8の既定でignoreCaseで照合される。ここでは小文字に正規化する
        username = username.toLowerCase(Locale.ROOT);
        Attempt current = attempts.get(username);
        // 現在時刻を取得
        // 内部でinstant()を何度も呼ぶと時間がずれるため先頭で1回取る
        Instant now = clock.instant();

        if (current == null || current.lockedUntil == null) {
            return;
        } else if (now.isBefore(current.lockedUntil)) {
            throw new LoginLockedException(
                    (Duration.between(now, current.lockedUntil).toMillis() + 999) / 1000);
        } else {
            // Mapの値がcurrentと一致する場合のみ削除する
            attempts.remove(username, current);
        }
    }

    @Override
    public void recordFailure(String username) {
        username = username.toLowerCase(Locale.ROOT);
        attempts.compute(username, (k, current) -> {
            // currentがnullの場合は初回失敗として1にする
            int failures = (current == null ? 0 : current.failures()) + 1;
            Instant lockedUntil = failures >= loginAttemptProperties.getMaxFailures()
                    ? clock.instant().plus(loginAttemptProperties.getLockDuration())
                    : null;
            return new Attempt(failures, lockedUntil);
        });
    }

    @Override
    public void recordSuccess(String username) {
        username = username.toLowerCase(Locale.ROOT);
        attempts.remove(username);
    }
}
