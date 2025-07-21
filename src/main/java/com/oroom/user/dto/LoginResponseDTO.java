package com.oroom.user.dto;

import com.oroom.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private User user;

    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;

    private String message;
    private boolean success;

}