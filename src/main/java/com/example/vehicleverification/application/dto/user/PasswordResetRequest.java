package com.example.vehicleverification.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {

    @NotBlank(message = "新しいパスワードは必須です")
    @Size(min = 8, message = "新しいパスワードは8文字以上で入力してください")
    private String newPassword;
}
