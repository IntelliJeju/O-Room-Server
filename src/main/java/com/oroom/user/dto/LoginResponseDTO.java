package com.oroom.user.dto;

import com.oroom.user.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private User user;
}
