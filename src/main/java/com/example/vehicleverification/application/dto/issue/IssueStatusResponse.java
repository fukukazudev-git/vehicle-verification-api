package com.example.vehicleverification.application.dto.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 指摘事項ステータスの語彙(enum名 + 日本語表示名)をクライアントへ公開するためのレスポンス
@Getter
@AllArgsConstructor
public class IssueStatusResponse {
    private String code; // IssueStatus の name()
    private String displayName; // IssueStatus の日本語表示名
}
