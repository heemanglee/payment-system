package com.example.assignment.point.user.entity;

import com.example.assignment.point.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "idx_unique_email", columnNames = {"email"})
})
@Entity
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Builder
    public User(String username, String email, String password, UserRole role, UserStatus userStatus) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.userStatus = userStatus;
    }
}
