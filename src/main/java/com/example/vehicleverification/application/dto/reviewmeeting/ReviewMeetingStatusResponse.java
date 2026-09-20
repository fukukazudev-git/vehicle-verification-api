package com.example.vehicleverification.application.dto.reviewmeeting;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 検証会ステータスの語彙(enum名 + 日本語表示名)をクライアントへ公開するためのレスポンス
@Getter
@AllArgsConstructor
public class ReviewMeetingStatusResponse {
    private String code; // ReviewMeetingStatus の name() (例: BEFORE_VERIFICATION)
    private String displayName; // 日本語表示名 (例: 検証前)
}
