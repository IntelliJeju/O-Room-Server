package com.savit.scheduler.job;

import com.savit.notification.service.NotificationService;
import com.savit.user.mapper.UserMapper;
import com.savit.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Random;

/**
 * 내부 메서드 호출 방식 - 랜덤 잔소리 알림 스케줄러
 * 오전 7시부터 다음날 오전 1시까지 랜덤한 시간에 잔소리 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RandomNaggingScheduler {

    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final Random random = new Random();


    /**
     * 테스트용 - 랜덤 잔소리 알림 (1분 후 딱 한번만 실행)
     * 서버 시작 1분 후에 딱 한번만 실행되는 테스트용 스케줄러
     */
    @Scheduled(initialDelay = 60000,  fixedDelay = Long.MAX_VALUE) // 1분 후 실행, 한번만 (29만년후 다시실행)
    public void sendRandomNaggingNotificationsForTest() {
        log.info("===== 테스트용 랜덤 잔소리 알림 스케줄러 시작 (딱 한번만 실행) =====");

        try {
            // 1. FCM 토큰이 등록된 활성 사용자 목록 조회
            List<User> activeUsers = userMapper.findUsersWithFcmTokens();

            if (activeUsers.isEmpty()) {
                log.info("FCM 토큰이 등록된 사용자가 없습니다.");
                return;
            }

            log.info("테스트용 랜덤 잔소리 알림 대상 사용자: {}명", activeUsers.size());

            // 2. 각 사용자별로 100% 확률로 잔소리 알림 발송 (테스트용)
            int sentCount = 0;
            for (User user : activeUsers) {
                try {
                    // 100% 확률로 알림 발송 (테스트용)
                    notificationService.sendRandomNaggingNotification(user.getId());
                    sentCount++;

                    // 알림 발송 간격 조절 (300ms 대기)
                    Thread.sleep(300);

                } catch (Exception e) {
                    log.error("사용자 {} 테스트용 랜덤 잔소리 알림 발송 실패", user.getId(), e);
                }
            }

            log.info("===== 테스트용 랜덤 잔소리 알림 스케줄러 완료 - 대상: {}명, 발송: {}명 (딱 한번 실행 완료) =====",
                    activeUsers.size(), sentCount);

        } catch (Exception e) {
            log.error("테스트용 랜덤 잔소리 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 운영용 실제 메서드
     * 내부 메서드 호출 방식 - 랜덤 잔소리 알림 (4시간마다)
     * 오전 8시부터 다음날 00시까지 4시간 간격으로 총 4번 실행
     * 08:00, 12:00, 16:00, 20:00에 실행
     */
    @Scheduled(cron = "0 0 8,12,16,20 * * *") // 08:00, 12:00, 16:00, 20:00에 실행
    public void sendRandomNaggingNotifications() {
        log.info("===== 랜덤 잔소리 알림 스케줄러 시작 (4시간마다) =====");

        try {
            // 1. FCM 토큰이 등록된 활성 사용자 목록 조회
            List<User> activeUsers = userMapper.findUsersWithFcmTokens();

            if (activeUsers.isEmpty()) {
                log.info("FCM 토큰이 등록된 사용자가 없습니다.");
                return;
            }

            log.info("랜덤 잔소리 알림 대상 사용자: {}명", activeUsers.size());

            // 2. 각 사용자별로 50% 확률로 잔소리 알림 발송
            int sentCount = 0;
            for (User user : activeUsers) {
                try {
                    // 50% 확률로 알림 발송
                    if (random.nextBoolean()) {
                        // 내부 메서드 호출 방식 - 랜덤 잔소리 알림 발송
                        notificationService.sendRandomNaggingNotification(user.getId());
                        sentCount++;

                        // 알림 발송 간격 조절 (500ms 대기)
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    log.error("사용자 {} 랜덤 잔소리 알림 발송 실패", user.getId(), e);
                }
            }

            log.info("===== 랜덤 잔소리 알림 스케줄러 완료 - 대상: {}명, 발송: {}명 =====",
                    activeUsers.size(), sentCount);

        } catch (Exception e) {
            log.error("랜덤 잔소리 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 내부 메서드 호출 방식 - 랜덤 하루 마무리 알림 (매일 오후 9시)
     * 하루를 마무리하는 시간에 푸시 발송
     */
    @Scheduled(cron = "0 0 21 * * *") // 매일 오후 9시
    public void sendDailyWrapUpNagging() {
        log.info("===== 하루 마무리 알림 시작 =====");

        try {
            List<User> activeUsers = userMapper.findUsersWithFcmTokens();

            if (activeUsers.isEmpty()) {
                log.info("FCM 토큰이 등록된 사용자가 없습니다.");
                return;
            }

            String[] wrapUpMessages = {
                "오늘 하루 신용카드는 덜 썼죠? 📝",
                "내일을 위해 오늘의 지출을 돌아봐요! 💭",
                "오늘 하루도 고생했어요! 내일도 신용카드 덜쓰기! ✅",
                "오늘 얼마나 썼는지 체크해볼까요? 💰"
            };

            int sentCount = 0;
            for (User user : activeUsers) {
                try {
                    // 30% 확률로 하루 마무리 메시지 발송
                    if (random.nextInt(100) < 30) {
                        String message = wrapUpMessages[random.nextInt(wrapUpMessages.length)];
                        notificationService.sendNotificationToUser(user.getId(), "💬 Savit 한마디", message);
                        sentCount++;

                        Thread.sleep(300);
                    }
                } catch (Exception e) {
                    log.error("사용자 {} 하루 마무리 알림 발송 실패", user.getId(), e);
                }
            }

            log.info("===== 하루 마무리 잔소리 알림 완료 - 대상: {}명, 발송: {}명 =====",
                    activeUsers.size(), sentCount);

        } catch (Exception e) {
            log.error("하루 마무리 잔소리 알림 실행 중 오류 발생", e);
        }
    }
}