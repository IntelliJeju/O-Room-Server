package com.savit.budget.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetCategoryVO {
    private Long id;
    private BigDecimal targetAmount;
    private Long budgetId;
    private Long categoryId;
}
