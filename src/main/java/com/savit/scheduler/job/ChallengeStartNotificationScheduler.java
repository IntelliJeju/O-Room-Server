package com.savit.scheduler.job;

import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.challenge.mapper.ChallengeMapper;
import com.savit.challenge.service.ChallengeService;
import com.savit.notification.dto.ChallengeNotificationDTO;
import com.savit.notification.service.NotificationService;
import com.savit.user.domain.User;
import com.savit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 챌린지 시작 알림 스케줄러
 * - 매일 21:30: 내일 시작하는 챌린지 알림 발송
 * - 매일 07:30: 오늘 시작하는 챌린지 알림 발송
 * - 사용자별 참여 가능한 챌린지만 필터링하여 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStartNotificationScheduler {

    private final ChallengeMapper challengeMapper;
    private final ChallengeService challengeService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    /**
     * 내일 시작하는 챌린지 알림 발송 (매일 21:30)
     * 테스트용으로 일단 @Scheduled 주석처리
     */
    // @Scheduled(cron = "0 30 21 * * *")
    public void sendTomorrowChallengeStartNotifications() {
        log.info("===== 내일 시작 챌린지 알림 발송 시작 =====");

        try {
            // 1. 내일 시작하는 챌린지 조회
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<ChallengeNotificationDTO> tomorrowChallenges = challengeMapper.findByStartDate(tomorrow);

            log.info("내일({}) 시작하는 챌린지 수: {}개", tomorrow, tomorrowChallenges.size());

            if (tomorrowChallenges.isEmpty()) {
                log.info("내일 시작하는 챌린지가 없습니다.");
                return;
            }

            // 2. FCM 토큰이 등록된 모든 사용자 조회
            List<User> allUsers = userMapper.findUsersWithFcmTokens();
            log.info("FCM 토큰 등록된 사용자 수: {}명", allUsers.size());

            if (allUsers.isEmpty()) {
                log.info("FCM 토큰이 등록된 사용자가 없습니다.");
                return;
            }

            int totalSentCount = 0;

            // 3. 각 사용자별로 참여 가능한 챌린지 확인 및 알림 발송
            for (User user : allUsers) {
                Long userId = user.getId();
                try {
                    // 3-1. 해당 사용자가 볼 수 있는 챌린지 목록 조회
                    List<ChallengeListDTO> userAvailableChallenges = challengeService.getChallengeList(userId);

                    log.info("사용자 {} 참여 가능한 챌린지 수: {}개", userId, userAvailableChallenges.size());
                    for (ChallengeListDTO available : userAvailableChallenges) {
                        log.info("  - 사용자 {} 가능 챌린지 ID: {}, 제목: {}", userId, available.getChallengeId(), available.getTitle());
                    }

                    // 3-2. 내일 시작하는 챌린지 중 사용자가 볼 수 있는 것만 필터링
                    for (ChallengeNotificationDTO tomorrowChallenge : tomorrowChallenges) {
                        log.info("내일 시작 챌린지 ID: {}, 제목: {}", tomorrowChallenge.getChallengeId(), tomorrowChallenge.getTitle());

                        boolean canSeeChallenge = userAvailableChallenges.stream()
                                .anyMatch(available -> available.getChallengeId().equals(tomorrowChallenge.getChallengeId()));

                        log.info("사용자 {} 챌린지 '{}' 참여 가능 여부: {}", userId, tomorrowChallenge.getTitle(), canSeeChallenge);

                        if (canSeeChallenge) {
                            // 3-3. 알림 메시지 생성 및 발송
                            String title = "🚀 새로운 챌린지 시작!";
                            String message = createChallengeStartMessage(tomorrowChallenge, tomorrow);

                            notificationService.sendNotificationToUser(userId, title, message);
                            totalSentCount++;

                            log.debug("사용자 {} 챌린지 '{}' 시작 알림 발송 완료",
                                    userId, tomorrowChallenge.getTitle());

                            // 발송 간격 조절
                            Thread.sleep(100);
                        }
                    }

                } catch (Exception e) {
                    log.error("사용자 {} 챌린지 시작 알림 발송 중 오류 발생", userId, e);
                }
            }

            log.info("===== 내일 시작 챌린지 알림 발송 완료: 총 {}건 발송 =====", totalSentCount);

        } catch (Exception e) {
            log.error("챌린지 시작 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 오늘 시작하는 챌린지 알림 발송 (매일 07:30)
     * 테스트용으로 일단 @Scheduled 주석처리
     */
    // @Scheduled(cron = "0 30 7 * * *")
    public void sendTodayChallengeStartNotifications() {
        log.info("===== 오늘 시작 챌린지 알림 발송 시작 =====");

        try {
            // 1. 오늘 시작하는 챌린지 조회
            LocalDate today = LocalDate.now();
            List<ChallengeNotificationDTO> todayChallenges = challengeMapper.findByStartDate(today);

            log.info("오늘({}) 시작하는 챌린지 수: {}개", today, todayChallenges.size());

            if (todayChallenges.isEmpty()) {
                log.info("오늘 시작하는 챌린지가 없습니다.");
                return;
            }

            // 2. FCM 토큰이 등록된 모든 사용자 조회
            List<User> allUsers = userMapper.findUsersWithFcmTokens();

            int totalSentCount = 0;

            // 3. 각 사용자별로 참여 가능한 챌린지 확인 및 알림 발송
            for (User user : allUsers) {
                Long userId = user.getId();
                try {
                    List<ChallengeListDTO> userAvailableChallenges = challengeService.getChallengeList(userId);

                    for (ChallengeNotificationDTO todayChallenge : todayChallenges) {
                        boolean canSeeChallenge = userAvailableChallenges.stream()
                                .anyMatch(available -> available.getChallengeId().equals(todayChallenge.getChallengeId()));

                        if (canSeeChallenge) {
                            String title = "🎯 챌린지 시작!";
                            String message = String.format("'%s' 챌린지가 오늘부터 시작됩니다! 지금 바로 참여해보세요! 💪",
                                    todayChallenge.getTitle());

                            notificationService.sendNotificationToUser(userId, title, message);
                            totalSentCount++;

                            log.debug("사용자 {} 챌린지 '{}' 당일 시작 알림 발송 완료",
                                    userId, todayChallenge.getTitle());

                            Thread.sleep(100);
                        }
                    }

                } catch (Exception e) {
                    log.error("사용자 {} 챌린지 당일 시작 알림 발송 중 오류 발생", userId, e);
                }
            }

            log.info("===== 오늘 시작 챌린지 알림 발송 완료: 총 {}건 발송 =====", totalSentCount);

        } catch (Exception e) {
            log.error("챌린지 당일 시작 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 챌린지 시작 알림 메시지 생성
     */
    private String createChallengeStartMessage(ChallengeNotificationDTO challenge, LocalDate startDate) {
        String formattedDate = startDate.format(DateTimeFormatter.ofPattern("M월 d일"));
        return String.format("'%s' 챌린지가 %s에 시작됩니다! 준비되셨나요? 🔥",
                challenge.getTitle(), formattedDate);
    }
}