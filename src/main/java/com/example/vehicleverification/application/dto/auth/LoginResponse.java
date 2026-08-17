package com.example.vehicleverification.application.dto.auth;

import lombok.Getter;
import lombok.Setter;

// ログイン応答用DTO
@Getter
@Setter
public class LoginResponse {

    private String token;

    private String username;

    private String role;

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

}
