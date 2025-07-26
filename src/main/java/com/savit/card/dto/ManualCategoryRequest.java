package com.savit.card.dto;

import lombok.Data;

/**
 * 사용자가 카드 승인 내역의 카테고리를 직접 수정할 때 사용하는 요청 DTO
 */
@Data
public class ManualCategoryRequest {
    private Long transactionId; // 수정할 거래 내역의 ID
    private Long categoryId;      // 사용자가 선택한 카테고리 ID
}