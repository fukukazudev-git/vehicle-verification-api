package com.example.vehicleverification.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserUpdateResponse {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private String department;
    private LocalDateTime createdAt;

    public UserUpdateResponse(Long id, String username, String displayName, String role, String department,
            LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.department = department;
        this.createdAt = createdAt;
    }

}
