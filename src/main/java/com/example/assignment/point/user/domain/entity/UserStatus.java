package com.example.assignment.point.user.domain.entity;

public enum UserStatus {

    ACTIVE("활성"),
    SUSPENDED("중지"),
    WITHDRAWN("탈퇴");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }
}
