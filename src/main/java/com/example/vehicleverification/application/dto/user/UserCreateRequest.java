package com.example.vehicleverification.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(max = 255, message = "パスワードは255文字以内で入力してください")
    private String password;

    @NotBlank(message = "表示名は必須です")
    @Size(max = 100, message = "表示名は100文字以内で入力してください")
    private String displayName;

    @NotBlank(message = "ロールは必須です")
    private String role;

}
