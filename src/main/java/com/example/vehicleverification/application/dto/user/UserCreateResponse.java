package com.example.vehicleverification.application.dto.user;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateResponse {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private LocalDateTime createdAt;

    public UserCreateResponse(Long id, String username, String displayName, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
    }

}
