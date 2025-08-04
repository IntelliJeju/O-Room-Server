package com.savit.scheduler.job;

import com.savit.card.mapper.CardTransactionMapper;
import com.savit.notification.domain.DailyTopSpending;
import com.savit.notification.mapper.DailyTopSpendingMapper;
import com.savit.notification.service.NotificationService;
import com.savit.user.domain.User;
import com.savit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 일일 최고 지출 알림 스케줄러
 * - 00:15: 전날 최고 지출 데이터 수집
 * - 09:00: 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTopSpendingScheduler {

    private final CardTransactionMapper cardTransactionMapper;
    private final DailyTopSpendingMapper dailyTopSpendingMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    /**
     * 실제 운영용 코드임
     * 전날 최고 지출 데이터 수집 (매일 00:15)
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void collectDailyTopSpending() {
        log.info("===== 일일 최고 지출 데이터 수집 시작 =====");

        try {
            // 전날 날짜 (YYYYMMDD 형식)
            String yesterday = LocalDate.now().minusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.info("수집 대상 날짜: {}", yesterday);

            // FCM 토큰이 있는 활성 사용자 목록 조회
            List<User> activeUsers = userMapper.findUsersWithFcmTokens();
            log.info("대상 사용자 수: {}명", activeUsers.size());

            int savedCount = 0;
            for (User user : activeUsers) {
                try {
                    // 중복 체크
                    if (dailyTopSpendingMapper.existsByUserAndDate(user.getId(), yesterday)) {
                        log.debug("사용자 {}의 {} 데이터 이미 존재", user.getId(), yesterday);
                        continue;
                    }

                    // 전날 최고 지출 항목 조회
                    Map<String, Object> topSpending = cardTransactionMapper
                            .findTopSpendingByUserAndDate(user.getId(), yesterday);

                    if (topSpending != null && !topSpending.isEmpty()) {
                        // DailyTopSpending 객체 생성 및 저장
                        DailyTopSpending dailyData = DailyTopSpending.builder()
                                .userId(user.getId())
                                .targetDate(yesterday)
                                .categoryName((String) topSpending.get("categoryName"))
                                .amount(new BigDecimal((String) topSpending.get("res_used_amount")))
                                .storeName((String) topSpending.get("res_member_store_name"))
                                .build();

                        dailyTopSpendingMapper.insertDailyTopSpending(dailyData);
                        savedCount++;
                        log.debug("사용자 {} 최고 지출 저장: {} - {}원", 
                                user.getId(), dailyData.getCategoryName(), dailyData.getAmount());
                    }

                } catch (Exception e) {
                    log.error("사용자 {} 데이터 수집 실패", user.getId(), e);
                }
            }

            log.info("===== 일일 최고 지출 데이터 수집 완료: {}건 저장 =====", savedCount);

        } catch (Exception e) {
            log.error("일일 최고 지출 데이터 수집 중 오류 발생", e);
        }
    }

    /**
     * 실제 운영용 코드임
     * 일일 최고 지출 알림 발송 (매일 09:00)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyTopSpendingNotifications() {
        log.info("===== 일일 최고 지출 알림 발송 시작 =====");

        try {
            // 알림 미발송 데이터 조회
            List<DailyTopSpending> pendingNotifications = dailyTopSpendingMapper.findPendingNotifications();
            log.info("발송 대상: {}건", pendingNotifications.size());

            int sentCount = 0;
            for (DailyTopSpending data : pendingNotifications) {
                try {
                    // 알림 메시지 생성
                    String message = String.format("너 어제 %s에 %s원이나 썼어! 이게 맞아? 💸", 
                            data.getCategoryName(), 
                            String.format("%,d", data.getAmount().intValue()));

                    // FCM 알림 발송
                    notificationService.sendNotificationToUser(
                            data.getUserId(), 
                            "💰 어제의 지출 TOP 1",
                            message
                    );

                    // 발송 완료 처리
                    dailyTopSpendingMapper.markNotificationSent(data.getId());
                    sentCount++;

                    log.info("사용자 {} 알림 발송 완료: {}", data.getUserId(), message);

                    // 발송 간격 조절
                    Thread.sleep(500);

                } catch (Exception e) {
                    log.error("사용자 {} 알림 발송 실패", data.getUserId(), e);
                }
            }

            log.info("===== 일일 최고 지출 알림 발송 완료: {}건 발송 =====", sentCount);

        } catch (Exception e) {
            log.error("일일 최고 지출 알림 발송 중 오류 발생", e);
        }
    }
}