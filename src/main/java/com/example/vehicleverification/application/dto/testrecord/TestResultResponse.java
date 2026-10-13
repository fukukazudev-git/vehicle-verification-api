package com.example.vehicleverification.application.dto.testrecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 試験結果の語彙(enum名 + 日本語表示名)をクライアントへ公開するためのレスポンス
@Getter
@AllArgsConstructor
public class TestResultResponse {
    private String code; // TestResult の name()
    private String displayName; // TestResult の日本語表示名
}
