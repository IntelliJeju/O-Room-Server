package com.savit.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일일 최고 지출 데이터 저장용 도메인
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTopSpending {
    private Long id;
    private Long userId;
    private String targetDate; // YYYYMMDD 형식
    private String categoryName;
    private BigDecimal amount;
    private String storeName;
    private LocalDateTime createdAt;
    private boolean notificationSent; // 알림 발송 여부
}