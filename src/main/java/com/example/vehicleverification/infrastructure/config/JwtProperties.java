package com.example.vehicleverification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "jwt")
@Component
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long expiration;

}
