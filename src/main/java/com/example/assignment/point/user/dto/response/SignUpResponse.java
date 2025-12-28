package com.example.assignment.point.user.dto.response;

import com.example.assignment.point.user.domain.entity.User;

public record SignUpResponse(
    Long userId
) {

    public static SignUpResponse fromEntity(User user) {
        return new SignUpResponse(user.getId());
    }
}
