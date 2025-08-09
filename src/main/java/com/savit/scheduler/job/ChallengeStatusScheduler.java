package com.savit.scheduler.job;

//  챌린지 상태 체크 스케쥴러
// 매일 00:00 시에 실행되어 종료된 챌린지의 성공자 처리

import com.savit.challenge.service.ChallengeCompletionService;
import com.savit.challenge.service.ChallengeProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStatusScheduler {
    private final ChallengeCompletionService challengeCompletionService;

    // 챌린지 완료 처리 - 매일 00:00 시에 실행
    // 종료일이 된 챌린지의 PARTICIPATING 참여자들을 SUCCESS 로 변경


    @Scheduled(cron = "0 0 0 * * *")
    public void checkCompletedChallenges() {
        log.info("===========챌린지 완료 처리 스케쥴러 시작 =========");

        try {
            // 완료된 챌린지들의 성공자 처리
            challengeCompletionService.processCompletedChallenges();
            log.info("==== 챌린지 완료 처리 스케쥴러 완료 ==========");
        } catch (Exception e ) {
            log.error("챌린지 완료 처리 스케쥴러 실행 중 오류 발생", e);
        }
    }

}
