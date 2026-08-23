package com.example.vehicleverification.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "ユーザ名は必須です")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    private String password;
}
