package com.example.vehicleverification.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    private String username;
    private String password;
    private String displayName;
    private String role;

    public UserCreateRequest(String username, String password, String displayName, String role) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
    }

}
