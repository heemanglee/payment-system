package com.example.assignment.point.user.dto.response;

import com.example.assignment.point.user.entity.User;

public record CreateUserResponse(
    Long userId
) {

    public static CreateUserResponse fromEntity(User user) {
        return new CreateUserResponse(user.getId());
    }
}
