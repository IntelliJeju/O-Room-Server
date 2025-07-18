package com.oroom.user.service;

import com.oroom.user.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO kakaoLogin(String code);
}
