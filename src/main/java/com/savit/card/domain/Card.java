package com.savit.card.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Card {

    private Long id;
    private String connectedId;
    private String organization;
    private String cardName;
    private String issuer;
    private String resCardNo;
    private String cardPassword;
    private String resCardType;
    private String resSleepYn;
    private LocalDateTime registeredAt;
    private Long userId;
}
