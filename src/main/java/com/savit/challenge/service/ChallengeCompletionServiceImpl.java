package com.savit.challenge.service;

import com.savit.challenge.domain.ChallengeVO;
import com.savit.challenge.dto.ParticipationStatusDTO;
import com.savit.challenge.mapper.ChallengeMapper;
import com.savit.challenge.mapper.ChallengeParticipationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeCompletionServiceImpl implements ChallengeCompletionService{

    //챌린지 완료 처리 서비스
    // 종료일이 된 챌린지의 성공자 처리 ( PARTICIPATING -> SUCCESS)

    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipationMapper challengeParticipationMapper;


    @Override
    @Transactional
    public void processCompletedChallenges() {
        try {
            log.info(" ===== 챌린지 완료 처리 시작 ==== ");

            // 1. 오늘 종료되는 챌린지들 조회
            List<ChallengeVO> completedChallenges = findChallengesEndingToday();

            if(completedChallenges.isEmpty()){
                log.info("오늘 종료되는 챌린지 없음");
                return;
            }
            log.info("오늘 종료되는 챌린지 : {} 개", completedChallenges.size());

            // 2. 각 챌린지별로 성공자 처리
            int totalProcessParticipants = 0;
            for(ChallengeVO challenge : completedChallenges){
                try {
                    int processedCount  = processSingleChallenge(challenge);
                    totalProcessParticipants += processedCount;
                }catch (Exception e) {
                    log.error("챌린지 완료 처리 실패 - 챌린지 ID :{}", challenge.getId(), e);
                    // 개별 챌린지 실패가 전체 중단시키지 않도록 continue ~~~
                }
            }
            log.info("==== 챌린지 완료 처리 완료 - 처리 챌린지 :{} 개, 성공자 :{} 명 =======", completedChallenges.size(),totalProcessParticipants);
        } catch (Exception e) {
            log.error("챌린지 완료 처리 중 전체 오류 발생", e);
            throw new RuntimeException("챌린지 완료 처리 실패", e);
        }

    }

    // 오늘 종료되는 챌린지 조회
    @Override
    public List<ChallengeVO> findChallengesEndingToday() {
        String today = LocalDate.now().toString();
        return challengeMapper.findChallengesEndingOnDate(today);
    }

    // 단일 챌린지의 성공자 처리
    @Override
    @Transactional
    public int processSingleChallenge(ChallengeVO challenge) {
        try {
            log.info("챌린지 완료 처리 시작 - 챌린지 ID : {}, 제목 :{}" , challenge.getId(), challenge.getTitle());

            // 1. 해당 챌린지의 진행중인 참여자들 조회
            List<ParticipationStatusDTO> participatingUsers = findParticipatingUsers(challenge.getId());

            if(participatingUsers.isEmpty()) {
                log.info("챌린지 {} - 진행중인 참여자가 없습니다.", challenge.getId());
                return  0;
            }
            log.info("챌린지 {} - 진행중인 참여자 : {}명", challenge.getId(), participatingUsers.size());

            // 2. 모든 진행중인 참여자를 성공을 ㅗ처리
            List<Long> participationIds = participatingUsers.stream().map(ParticipationStatusDTO::getParticipationId).collect(Collectors.toList());

            updateSuccessfulParticipants(participationIds);

            log.info("챌린지 완료 처리 완료 - 챌린지ID: {}, 성공자 : {} 명 ", challenge.getId(), participationIds.size());

            return participationIds.size();
        } catch (Exception e){
            log.error("단일 챌린지 완료 처리 실패 - 챌린지ID : {}", challenge.getId(),e);
            throw e;
        }
    }


    // 특정 챌린지의 진행중인 참여자들 조회
    @Override
    public List<ParticipationStatusDTO> findParticipatingUsers(Long challengeId) {
        return challengeParticipationMapper.findParticipatingUsersByChallengeId(challengeId);
    }

    // 참여자들을 success 상태로 일괄 업데이트
    @Override
    public void updateSuccessfulParticipants(List<Long> participationIds) {
        if (participationIds.isEmpty()){
            return;
        }
        String completedAt  = LocalDateTime.now().toString();
        challengeParticipationMapper.updateStatusToSuccess(participationIds, completedAt);

        log.debug("성공 처리 완료 - 대상자: {} 명", participationIds.size());
    }

    // 특정 챌린지의 완료 처리 - 수동 호출
    @Override
    public int processSpecificChallenge(Long challengeId) {
        ChallengeVO challenge  = challengeMapper.findById(challengeId);
        if( challenge == null ) {
            log.warn("존재하지 않은 챌린지 - ID :{} " , challengeId);
            return  0;
        }
        return processSingleChallenge(challenge);
    }
}
