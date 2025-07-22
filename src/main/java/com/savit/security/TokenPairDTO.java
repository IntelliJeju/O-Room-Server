package com.savit.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPairDTO {

    private String accessToken;
    private String refreshToken;

    // 토큰 만료 시간 정보
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
}