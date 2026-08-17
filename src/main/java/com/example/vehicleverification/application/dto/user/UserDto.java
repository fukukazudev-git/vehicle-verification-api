package com.example.vehicleverification.application.dto.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 一覧用
@Getter
@Setter
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private LocalDateTime createdAt;
    private String department;
}
