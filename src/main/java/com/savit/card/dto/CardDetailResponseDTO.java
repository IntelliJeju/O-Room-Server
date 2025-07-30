package com.savit.card.dto;

import com.savit.card.domain.Card;
import lombok.Getter;

@Getter
public class CardDetailResponseDTO {
    private Long cardId;
    private String cardName;
    private String resCardNo;
    private int usageAmount;
    private String resImageLink;

    public CardDetailResponseDTO(Card card, int usageAmount) {
        this.cardId = card.getId();
        this.cardName = card.getCardName();
        this.resCardNo = card.getResCardNo();
        this.usageAmount = usageAmount;
        this.resImageLink = card.getResImageLink();
    }
}
