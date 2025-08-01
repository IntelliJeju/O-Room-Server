package com.savit.budget.service;

import com.savit.card.dto.BudgetMonitoringDTO;
import com.savit.card.service.CardApprovalService;
import com.savit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 내부 메서드 호출 방식 - 예산 모니터링 서비스
 * 스케줄러에서 카드 승인내역 동기화 후 예산 체크 및 알림 발송을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetMonitoringService {
    
    private final CardApprovalService cardApprovalService;
    private final NotificationService notificationService;
    
    /**
     * 내부 메서드 호출 방식 - 예산 체크 및 알림 발송
     * 카드 승인내역 동기화 후 호출되어 예산 초과 여부를 확인하고 알림을 발송
     */
    public void checkBudgetAndSendNotifications(Long userId) {
        try {
            log.info("예산 모니터링 시작 - 사용자: {}", userId);
            
            // 내부 메서드 호출 방식 - 예산 모니터링 데이터 조회
            BudgetMonitoringDTO data = cardApprovalService.getBudgetMonitoringData(userId);
            
            // 예산이 설정되지 않은 경우 스킵
            if (!data.isHasBudget()) {
                log.debug("사용자 {}는 예산이 설정되지 않아 모니터링을 건너뜁니다.", userId);
                return;
            }
            
            // 금액 포맷팅용
            NumberFormat formatter = NumberFormat.getInstance(Locale.KOREA);
            
            // 1. 예산 100% 초과 시 알림 (우선순위 높음)
            if (data.isOverBudget()) {
                BigDecimal exceededAmount = data.getThisMonthUsage().subtract(data.getTotalBudget());
                String exceededAmountStr = formatter.format(exceededAmount) + "원";
                String totalBudgetStr = formatter.format(data.getTotalBudget()) + "원";
                
                notificationService.sendBudgetExceededNotification(userId, exceededAmountStr, totalBudgetStr);
                log.info("예산 초과 알림 발송 완료 - 사용자: {}, 초과금액: {}", userId, exceededAmountStr);
            }
            // 2. 예산 80% 이상 사용 시 경고 알림 (예산 초과가 아닌 경우에만)
            else if (data.isWarningLevel()) {
                BigDecimal usagePercent = data.getUsageRate().multiply(BigDecimal.valueOf(100));
                BigDecimal remainingAmount = data.getTotalBudget().subtract(data.getThisMonthUsage());
                String remainingAmountStr = formatter.format(remainingAmount) + "원";
                
                notificationService.sendCategoryBudgetWarning(
                    userId, 
                    "전체", 
                    usagePercent.intValue(), 
                    remainingAmountStr
                );
                log.info("예산 경고 알림 발송 완료 - 사용자: {}, 사용률: {}%", userId, usagePercent.intValue());
            }
            
            log.info("예산 모니터링 완료 - 사용자: {}, 예산여부: {}, 초과여부: {}, 경고여부: {}", 
                    userId, data.isHasBudget(), data.isOverBudget(), data.isWarningLevel());
            
        } catch (Exception e) {
            log.error("예산 모니터링 실패 - 사용자: {}", userId, e);
        }
    }
    
    /**
     * 내부 메서드 호출 방식 - 여러 사용자 일괄 예산 체크
     * 스케줄러에서 전체 사용자 처리 시 사용
     * 위에 있는 개별 처리 방식이 더 효율적이고 실시간성 좋아보임...
     * 일단 코드는 작성해두고 사용은 안하는걸로.
     */
    public void checkBudgetForMultipleUsers(java.util.List<Long> userIds) {
        log.info("다중 사용자 예산 모니터링 시작 - 대상 사용자: {}명", userIds.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long userId : userIds) {
            try {
                checkBudgetAndSendNotifications(userId);
                successCount++;
            } catch (Exception e) {
                log.error("사용자 {} 예산 모니터링 실패", userId, e);
                failCount++;
            }
        }
        
        log.info("다중 사용자 예산 모니터링 완료 - 성공: {}명, 실패: {}명", successCount, failCount);
    }
}
