package com.example.vehicleverification.domain.entity;

public enum TestResult {
    OK("OK"),
    NG("NG"),
    PENDING("保留");

    private final String displayName;

    TestResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
