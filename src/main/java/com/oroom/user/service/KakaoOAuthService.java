package com.oroom.user.service;

import com.oroom.user.dto.KakaoTokenDTO;
import com.oroom.user.dto.KakaoUserDTO;

public interface KakaoOAuthService {
    KakaoTokenDTO getAccessToken(String code);
    KakaoUserDTO getUserInfo(String accessToken);
}