package com.savit.user.service;

import com.savit.user.dto.KakaoTokenDTO;
import com.savit.user.dto.KakaoUserDTO;

public interface KakaoOAuthService {
    KakaoTokenDTO getAccessToken(String code);
    KakaoUserDTO getUserInfo(String accessToken);
}