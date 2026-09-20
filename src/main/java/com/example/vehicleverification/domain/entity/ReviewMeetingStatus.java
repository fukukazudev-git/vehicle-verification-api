package com.example.vehicleverification.domain.entity;

public enum ReviewMeetingStatus {
    BEFORE_VERIFICATION("検証前"),
    IN_VERIFICATION("検証中"),
    COMPLETED("完了"),
    CANCELLED("中断");

    private final String displayName;

    ReviewMeetingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
