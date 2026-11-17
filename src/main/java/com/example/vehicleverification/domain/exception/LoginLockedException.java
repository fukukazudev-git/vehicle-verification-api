package com.example.vehicleverification.domain.exception;

import lombok.Getter;

@Getter
public class LoginLockedException extends RuntimeException {
    private final long retryAfterSeconds;

    public LoginLockedException(long retryAfterSeconds) {
        super("Login is locked. Retry after " + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
