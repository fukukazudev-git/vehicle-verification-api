package com.example.vehicleverification.infrastructure.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "login-attempt")
@Component
@Getter
@Setter
public class LoginAttemptProperties {
    private int maxFailures;
    private Duration lockDuration;
}
