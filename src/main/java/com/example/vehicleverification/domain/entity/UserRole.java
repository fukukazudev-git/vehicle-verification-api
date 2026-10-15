package com.example.vehicleverification.domain.entity;

public enum UserRole {
    ADMIN("管理者"),
    MEMBER("メンバー");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
