package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.vehicleverification.domain.exception.LoginLockedException;
import com.example.vehicleverification.infrastructure.config.LoginAttemptProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceImplTest {

    // 基準の時刻を設定
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private Clock clock;

    private LoginAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        // 振る舞いを毎回差し替える必要がないため、実物をインスタンス化して使用する
        LoginAttemptProperties properties = new LoginAttemptProperties();
        properties.setMaxFailures(5);
        properties.setLockDuration(Duration.ofMinutes(15));

        service = new LoginAttemptServiceImpl(clock, properties);
    }

    // 実際のログインと同じく「checkLocked → 失敗の記録」の順で、指定回数の失敗をシミュレートする
    private void failLogin(String username, int times) {
        for (int i = 0; i < times; i++) {
            service.checkLocked(username);
            service.recordFailure(username);
        }
    }

    @Test
    void checkLocked_4回ログイン失敗_例外を投げない() {
        given(clock.instant()).willReturn(T0);

        failLogin("username", 4);

        assertThatCode(() -> service.checkLocked("username")).doesNotThrowAnyException();
    }

    @Test
    void checkLocked_5回ログイン失敗_ロック時間の秒数を持つ例外を投げる() {
        given(clock.instant()).willReturn(T0);

        failLogin("username", 5);

        // Retry-Afterに使う残り秒数が、ロック時間(15分)と一致することまで確認する
        assertThatThrownBy(() -> service.checkLocked("username"))
                .isInstanceOf(LoginLockedException.class)
                .hasFieldOrPropertyWithValue("retryAfterSeconds", 900L);
    }

    @Test
    void checkLocked_ロック解除の直前_まだ例外を投げる() {
        given(clock.instant()).willReturn(T0);
        failLogin("username", 5);

        // 15分の1ミリ秒前は、まだロック中
        given(clock.instant()).willReturn(T0.plus(Duration.ofMinutes(15)).minusMillis(1));

        assertThatThrownBy(() -> service.checkLocked("username")).isInstanceOf(LoginLockedException.class);
    }

    @Test
    void checkLocked_残り1秒未満_残り秒数を切り上げて1を返す() {
        given(clock.instant()).willReturn(T0);
        failLogin("username", 5);

        // 残り0.5秒。切り捨てるとRetry-After: 0になってしまう
        given(clock.instant()).willReturn(T0.plus(Duration.ofMinutes(15)).minusMillis(500));

        assertThatThrownBy(() -> service.checkLocked("username"))
                .isInstanceOf(LoginLockedException.class)
                .hasFieldOrPropertyWithValue("retryAfterSeconds", 1L);
    }

    @Test
    void checkLocked_ロックから15分後_制限が解除され失敗回数もリセットされる() {
        given(clock.instant()).willReturn(T0);
        failLogin("username", 5);

        // 15分後に時刻を進める
        given(clock.instant()).willReturn(T0.plus(Duration.ofMinutes(15)));

        assertThatCode(() -> service.checkLocked("username")).doesNotThrowAnyException();

        // 解除後は0回から数え直すため、1回失敗しただけではロックされない
        service.recordFailure("username");
        assertThatCode(() -> service.checkLocked("username")).doesNotThrowAnyException();
    }

    @Test
    void checkLocked_ログイン成功で失敗カウントがリセットされる() {
        given(clock.instant()).willReturn(T0);

        failLogin("username", 4);

        // ログイン成功
        service.recordSuccess("username");

        // さらに4回失敗させて失敗カウントがリセットされていることを確認
        failLogin("username", 4);

        assertThatCode(() -> service.checkLocked("username")).doesNotThrowAnyException();
    }

    @Test
    void checkLocked_正規化によって大文字小文字が合算されてロックされる() {
        given(clock.instant()).willReturn(T0);

        // 大文字小文字を混ぜて失敗を記録
        failLogin("USERname", 1);
        failLogin("username", 1);
        failLogin("UserName", 1);
        failLogin("uSeRnAmE", 1);
        failLogin("Username", 1);

        // 合算でロックされることを確認
        assertThatThrownBy(() -> service.checkLocked("username")).isInstanceOf(LoginLockedException.class);
    }

    @Test
    void checkLocked_別のユーザー名の失敗_影響を受けない() {
        given(clock.instant()).willReturn(T0);

        failLogin("attacked", 5);

        // ユーザー名ごとに数えているため、ほかのユーザーはロックされない
        assertThatCode(() -> service.checkLocked("other")).doesNotThrowAnyException();
    }
}
