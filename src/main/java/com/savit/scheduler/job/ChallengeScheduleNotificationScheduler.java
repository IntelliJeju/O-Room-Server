package com.savit.scheduler.job;

import com.savit.notification.dto.ChallengeNotificationDTO;
import com.savit.challenge.mapper.ChallengeMapper;
import com.savit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduleNotificationScheduler {

    private final ChallengeMapper challengeMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 22 * * *") // 매일 밤 22시
    public void sendChallengeScheduleNotifications() {
        log.info("===== 챌린지 일정 알림 스케줄러 시작 =====");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        try {
            // 내일 시작 챌린지
            List<ChallengeNotificationDTO> starting = challengeMapper.findByStartDate(tomorrow);
            for (ChallengeNotificationDTO challenge : starting) {
                notify(challenge, true);
            }

            // 내일 종료 챌린지
            List<ChallengeNotificationDTO> ending = challengeMapper.findByEndDate(tomorrow);
            for (ChallengeNotificationDTO challenge : ending) {
                notify(challenge, false);
            }

            log.info("===== 챌린지 일정 알림 스케줄러 완료 =====");
        } catch (Exception e) {
            log.error("챌린지 일정 알림 스케줄러 오류 발생", e);
        }
    }

    private void notify(ChallengeNotificationDTO challenge, boolean isStart) {
        List<Long> userIds = challengeMapper.findUserIdsByChallengeId(challenge.getChallengeId());
        String title = isStart ?
                "🔥 [" + challenge.getTitle() + "] 내일 시작합니다!" :
                "⏰ [" + challenge.getTitle() + "] 내일 종료됩니다!";
        String body = isStart ?
                "준비되셨나요? 내일 새로운 도전이 시작됩니다 💪" :
                "내일이 마지막 날입니다! 목표를 꼭 달성하세요 🏁";

        for (Long userId : userIds) {
            notificationService.sendNotificationToUser(userId, title, body);
        }
    }

    @Scheduled(cron = "0 1 22 * * *") // 매일 22시 1분 스케쥴러 체크
    public void schedulerHealthCheck() {
        log.debug("챌린지 일정 알림 스케줄러 상태 체크 - 정상 동작 중");
    }
}
