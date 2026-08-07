package com.example.vehicleverification.application.dto.user;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

//一覧用
@Getter
@Setter
public class UserDto {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private LocalDateTime createdAt;
    private String department;

    public UserDto(Long id, String username, String displayName, String role, LocalDateTime createdAt,
            String department) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
        this.department = department;
    }

}
