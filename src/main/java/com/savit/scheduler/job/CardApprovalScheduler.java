package com.savit.scheduler.job;

import com.savit.card.service.AsyncCardApprovalService;
import com.savit.user.mapper.UserMapper;
import com.savit.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 내부 메서드 호출 방식 - 카드 승인내역 자동 동기화 스케줄러
 * 09:00, 12:00, 18:00, 00:00에 실행되어 모든 사용자의 카드 승인내역을 자동으로 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardApprovalScheduler {

    private final AsyncCardApprovalService asyncCardApprovalService;
    private final UserMapper userMapper;

    /**
     * 내부 메서드 호출 방식 - 카드 승인내역 자동 동기화 (하루 4회)
     * 08:00, 12:00, 18:00, 00:00에 실행
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 8,12,18,0 * * *")
    public void syncAllUsersCardApprovals() {
        log.info("===== 카드 승인내역 자동 동기화 스케줄러 시작 =====");

        try {
            // 1. 활성 사용자 목록 조회 (카드가 등록된 사용자만)
            List<User> activeUsers = userMapper.findUsersWithCards();

            if (activeUsers.isEmpty()) {
                log.info("카드가 등록된 활성 사용자가 없습니다. 스케줄러를 종료합니다.");
                return;
            }

            log.info("카드 승인내역 동기화 대상 사용자: {}명", activeUsers.size());

            // 2. 각 사용자별로 비동기 처리 시작
            List<Long> userIds = activeUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            int processedCount = 0;
            for (Long userId : userIds) {
                try {
                    // 내부 메서드 호출 방식 - 비동기로 사용자별 카드 승인내역 처리
                    asyncCardApprovalService.processUserCardApprovalsAsync(userId);
                    processedCount++;

                    // 외부 API 호출 부하 분산을 위한 대기시간 (5000ms)
                    Thread.sleep(5000);

                } catch (Exception e) {
                    log.error("사용자 {} 카드 승인내역 처리 시작 실패", userId, e);
                }
            }

            // 3. 처리 현황 로깅
            asyncCardApprovalService.logAsyncProcessingStatus(activeUsers.size(), processedCount);

            log.info("===== 카드 승인내역 자동 동기화 스케줄러 완료 - 처리 시작: {}명 =====", processedCount);

        } catch (Exception e) {
            log.error("카드 승인내역 자동 동기화 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 내부 메서드 호출 방식 - 스케줄러 상태 체크 (매시간 정각)
     * 스케줄러가 정상 동작하는지 확인용 로그
     */
    @Scheduled(cron = "0 0 * * * *")
    public void schedulerHealthCheck() {
        log.debug("카드 승인내역 스케줄러 상태 체크 - 정상 동작 중");
    }
}