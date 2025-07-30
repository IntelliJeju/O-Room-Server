package com.savit.card.dto;

import lombok.Data;

@Data
public class CardTransactionDto {
    private String resMemberStoreName;
    private String resUsedAmount;
    private String resUsedDate;
    private String resUsedTime;
    private String resMemberStoreType;
    private Long categoryId;
    private Long cardId;
    private Long userId;
}
