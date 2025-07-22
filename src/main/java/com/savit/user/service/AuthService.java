package com.savit.user.service;

import com.savit.user.dto.LoginResponseDTO;

public interface AuthService {

    /**
     * 카카오 로그인 처리
     * @param code 카카오에서 받은 Authorization Code
     * @return 로그인 응답 (JWT 토큰 + 사용자 정보)
     */
    LoginResponseDTO kakaoLogin(String code);

    /**
     * Refresh Token으로 새로운 Access Token 발급
     * @param refreshToken 갱신용 토큰
     * @return 새로운 토큰 정보
     */
    LoginResponseDTO refreshAccessToken(String refreshToken);
}