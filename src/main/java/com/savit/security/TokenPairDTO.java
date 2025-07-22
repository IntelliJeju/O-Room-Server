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

    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
}