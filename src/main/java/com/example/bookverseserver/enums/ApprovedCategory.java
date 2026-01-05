package com.example.bookverseserver.enums;

import lombok.Getter;

@Getter
public enum ApprovedCategory {
    FICTION("Fiction"),
    NON_FICTION("Non-fiction"),
    SCIENCE("Science"),
    TECHNOLOGY("Technology"),
    BUSINESS("Business"),
    SELF_HELP("Self-Help");

    private final String displayName;

    ApprovedCategory(String displayName) {
        this.displayName = displayName;
    }
}