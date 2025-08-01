package com.savit.card.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 예산 모니터링에 필요한 데이터를 담는 DTO 클래스
 * CardApprovalService에서 생성하여 BudgetMonitoringService로 전달
 */
@Getter
@Builder
public class BudgetMonitoringDTO {

    private final Long userId;
    private final String currentMonth;
    private final BigDecimal totalBudget;
    private final BigDecimal thisMonthUsage;
    private final BigDecimal usageRate;
    private final boolean hasBudget;
    private final boolean isOverBudget;
    private final boolean isWarningLevel;

}
