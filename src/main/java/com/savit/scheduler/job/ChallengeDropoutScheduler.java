package com.savit.scheduler.job;

import com.savit.challenge.dto.ChallengeDropoutSummaryDTO;
import com.savit.challenge.mapper.ChallengeMapper;
import com.savit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 챌린지 일일 낙오 요약 알림 스케줄러
 * - 22:00: 일일 낙오 통계를 집계하여 모든 참여자에게 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeDropoutScheduler {

    private final ChallengeMapper challengeMapper;
    private final NotificationService notificationService;

    /**
     * 챌린지별 일일 낙오 요약 알림 발송 (매일 22:00)
     * 테스트용으로 일단 @Scheduled 주석처리
     * 실제 운영시 주석 해제해야함
     */
//    @Scheduled(cron = "0 0 22 * * *")
    public void sendDailyDropoutSummary() {
        log.info("===== 챌린지별 일일 낙오 요약 알림 발송 시작 =====");

        try {
            // 1. 진행 중인 챌린지별 낙오 통계 조회
            List<ChallengeDropoutSummaryDTO> challengeSummaries = challengeMapper.getChallengeDropoutSummaries();
            log.info("진행 중인 챌린지 수: {}개", challengeSummaries.size());

            if (challengeSummaries.isEmpty()) {
                log.info("현재 진행 중인 챌린지가 없습니다.");
                return;
            }

            int totalSentCount = 0;

            // 2. 각 챌린지별로 개별 처리
            for (ChallengeDropoutSummaryDTO summary : challengeSummaries) {
                try {
                    log.info("챌린지 '{}' 처리 시작 - 전체: {}, 낙오: {}, 활성: {}", 
                            summary.getChallengeTitle(), summary.getTotalParticipants(), 
                            summary.getDropoutCount(), summary.getActiveCount());

                    // 참여자가 없으면 해당 챌린지 건너뛰기
                    if (summary.getTotalParticipants() == null || summary.getTotalParticipants() == 0) {
                        log.info("챌린지 '{}'에 참여자가 없습니다.", summary.getChallengeTitle());
                        continue;
                    }

                    // 3. 해당 챌린지의 FCM 토큰 등록된 참여자 조회
                    List<Long> participantUserIds = challengeMapper.findParticipantUserIdsByChallengeId(summary.getChallengeId());
                    log.info("챌린지 '{}' 알림 발송 대상: {}명", summary.getChallengeTitle(), participantUserIds.size());

                    if (participantUserIds.isEmpty()) {
                        log.info("챌린지 '{}'에 FCM 토큰 등록된 참여자가 없습니다.", summary.getChallengeTitle());
                        continue;
                    }

                    // 4. 챌린지별 알림 메시지 생성
                    String message = createChallengeDropoutMessage(summary);
                    String title = "📊 " + summary.getChallengeTitle() + " 현황";

                    // 5. 해당 챌린지 참여자들에게 알림 발송
                    int sentCount = 0;
                    for (Long userId : participantUserIds) {
                        try {
                            notificationService.sendNotificationToUser(userId, title, message);
                            sentCount++;
                            totalSentCount++;

                            log.debug("사용자 {} 챌린지 '{}' 낙오 요약 알림 발송 완료", userId, summary.getChallengeTitle());

                            // 발송 간격 조절
                            Thread.sleep(300);

                        } catch (Exception e) {
                            log.error("사용자 {} 챌린지 '{}' 낙오 요약 알림 발송 실패", userId, summary.getChallengeTitle(), e);
                        }
                    }

                    log.info("챌린지 '{}' 알림 발송 완료: {}명", summary.getChallengeTitle(), sentCount);

                } catch (Exception e) {
                    log.error("챌린지 '{}' 처리 중 오류 발생", summary.getChallengeTitle(), e);
                }
            }

            log.info("===== 챌린지별 일일 낙오 요약 알림 발송 완료: 총 {}명 발송 =====", totalSentCount);

        } catch (Exception e) {
            log.error("챌린지별 일일 낙오 요약 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 챌린지별 낙오 요약 메시지 생성
     */
    private String createChallengeDropoutMessage(ChallengeDropoutSummaryDTO summary) {
        long totalParticipants = summary.getTotalParticipants() != null ? summary.getTotalParticipants() : 0;
        long dropoutCount = summary.getDropoutCount() != null ? summary.getDropoutCount() : 0;
        long activeCount = summary.getActiveCount() != null ? summary.getActiveCount() : 0;

        if (dropoutCount == 0) {
            return String.format("🎉 모든 참여자(%d명)가 잘 진행하고 있어요! 파이팅! 💪", 
                    totalParticipants);
        } else {
            return String.format("📈 전체 %d명 중 %d명 낙오, %d명 여전히 도전 중! 🔥", 
                    totalParticipants, dropoutCount, activeCount);
        }
    }
}