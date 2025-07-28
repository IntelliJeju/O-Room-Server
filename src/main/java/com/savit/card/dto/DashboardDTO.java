package com.savit.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardDTO {
    // 예산 정보 (Budget에서 가져온 데이터)
    private String budgetMonth;           // 예산 설정 월 "202501"
    private BigDecimal totalBudget;       // 설정한 총 한도 (Budget.totalBudget)

    // 사용 현황 (CardTransaction에서 계산)
    private BigDecimal thisMonthUsage;    // 이번달 실제 사용금액
    private BigDecimal lastMonthUsage;    // 저번달 실제 사용금액
    private BigDecimal remainingBudget;   // 남은 한도
    private BigDecimal usageRate;         // 사용률 (%)
    private BigDecimal dailyAverage;      // 일평균 사용금액

    // 기간 정보
    private String currentMonth;          // 현재 월 "202501"
    private int daysInMonth;             // 이번달 총 일수
    private int daysPassed;              // 지난 일수

    // 예산 상태
    private boolean hasBudget;           // 예산 설정 여부
    private boolean isOverBudget;        // 예산 초과 여부
}
