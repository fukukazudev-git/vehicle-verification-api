package com.example.vehicleverification.application.service;

public interface LoginAttemptService {
    void checkLocked(String username);

    void recordFailure(String username);

    void recordSuccess(String username);
}
