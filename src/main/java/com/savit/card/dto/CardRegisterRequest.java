package com.savit.card.dto;

import lombok.Data;

@Data
public class CardRegisterRequest {
    private String organization;
    private String loginId;
    private String loginPw;
    private String birthDate;
    private String cardPassword;
}
