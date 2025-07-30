package com.savit.card.domain;

import lombok.Data;

@Data
public class CardTransactionVO {
    private Long id;
    private Long cardId;

    private String resCardNo;
    private String resUsedDate;
    private String resUsedTime;
    private String resUsedAmount;
    private String resCancelYn;
    private String resCancelAmount;
    private String resTotalAmount;

    private Long budgetCategoryId;
    private Long categoryId;

    private String resMemberStoreName;
    private String resMemberStoreType;

    private String createdAt;
    private String updatedAt;
}
