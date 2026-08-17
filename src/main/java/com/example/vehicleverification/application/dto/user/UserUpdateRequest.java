package com.example.vehicleverification.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "表示名は必須です")
    @Size(max = 50, message = "表示名は50文字以内で入力してください")
    private String displayName;

    @NotBlank(message = "ロールは必須です")
    private String role;

    @NotBlank(message = "部署は必須です")
    @Size(max = 50, message = "部署は50文字以内で入力してください")
    private String department;
}
