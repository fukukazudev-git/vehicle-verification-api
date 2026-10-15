package com.example.vehicleverification.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

// ユーザーロールの語彙(enum名 + 日本語表示名)をクライアントへ公開するためのレスポンス
@Getter
@AllArgsConstructor
public class UserRoleResponse {

    private String code; // UserRoleの name()
    private String displayName; // UserRoleの日本語表示名
}
