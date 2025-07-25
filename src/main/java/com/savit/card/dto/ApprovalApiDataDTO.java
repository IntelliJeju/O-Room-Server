package com.savit.card.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApprovalApiDataDTO {
    // User Table
    private String birthDate;

    // Card Table
    private String connectedId;
    private String organization;
//    private String encryptedCardNo;       // 암호화된 카드번호 -> 이거 말고 res_card_no 써야함
    private String resCardNo;
    private String cardPassword;    // 암호화된 카드 비밀번호
    private String cardName;
}