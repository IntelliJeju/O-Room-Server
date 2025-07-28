package com.savit.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetCategorySearchDTO {
    // 프론트에서 월과 카테고리 입력 받는 용
    private List<String> months;
    private List<Long> categoryIds;
}