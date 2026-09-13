package com.example.vehicleverification.domain.entity;

public enum IssueStatus {
    UNRESOLVED("未対応"),
    IN_PROGRESS("対応中"),
    PENDING_APPROVAL("承認待ち"),
    RESOLVED("完了");

    private final String displayName;

    IssueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
