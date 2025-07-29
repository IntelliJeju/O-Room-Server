package com.savit.card.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CardRegisterRequestDTO {
    private String organization;
    private String loginId;
    private String loginPw;
    private String birthDate;
    private String cardPassword;
    private String encryptedCardNo;
}
