package com.example.vehicleverification.application.dto.user;

import com.example.vehicleverification.domain.entity.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCreateResponse {

    private Long id;
    private String username;
    private String displayName;
    private UserRole role;
    private LocalDateTime createdAt;
    private String department;
}
