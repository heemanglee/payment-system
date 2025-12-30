package com.example.assignment.point.user.domain.service;

public interface RegistrationService {

    /**
     * 회원가입 완료 후 Redis에 이메일 저장
     *
     * @param email: 회원가입 완료된 이메일
     */
    void addEmail(String email);

    /**
     * 이메일 중복 여부 확인 (SISMEMBER)
     *
     * @param email: 중복 체크할 이메일
     * @return 존재 여부
     */
    boolean isEmailExists(String email);

}
