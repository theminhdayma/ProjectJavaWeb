package com.data.entity.enums;

public enum ResultInterview {
    PASSED("Đậu"),
    FAILED("Trượt");

    private final String displayName;
    ResultInterview(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
