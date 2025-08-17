package com.example.bookverseserver.enums;

import org.jetbrains.annotations.NotNull;

public enum PermissionName implements CharSequence {
    EDIT_BOOK, CREATE_ORDER, VIEW_REPORTS;

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }
}
