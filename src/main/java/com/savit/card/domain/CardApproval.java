package com.savit.card.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder
public class CardApproval {
    private Long id;        // 카드거래 ID

    // 사용자 관련
    private Long cardId;
    private Long userId;

    private String resCardNo;              // 카드번호 일부 마스킹된값
    private String resUsedDate;             // "20250723"
    private String resUsedTime;             // "113022"
    private String resUsedAmount;           // "5500"
    private String resCancelYN;             // "0" 또는 "1"
    private String resCancelAmount;         // 취소여부 1일때 금액나옴
    private String resTotalAmount;          // 신용 총 결제대금 (서비스에서 처리?)
    private String resMemberStoreName;      // 가맹점명 : ex "스타벅스강남점"
    private String resMemberStoreType;      // 업종명 : ex "카페"

    // 메타데이터
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정 일시

    private Long budgetCategoryId;      // 예산 카테고리 ID
    private Long categoryId;            // 카테고리 ID
}
