package com.savit.challenge.service;

import com.savit.card.domain.CardTransactionVO;
import com.savit.card.mapper.CardTransactionMapper;
import com.savit.challenge.dto.ChallengeProgressDTO;
import com.savit.challenge.dto.ChallengeUpdateRequestDTO;
import com.savit.challenge.dto.ParticipationStatusDTO;
import com.savit.challenge.mapper.ChallengeParticipationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeParticipationServiceImpl implements ChallengeParticipationService {

    /*챌린지 참여자 상태 업데이트 메인 서비스
     * 카드 동기화 시 챌린지 진행상황 업데이트*/

    private final CardTransactionMapper cardTransactionMapper;
    private final ChallengeParticipationMapper challengeParticipationMapper;
    private final ChallengeProgressService challengeProgressService;

    // 새로운 거래내애역에 대한 챌린지 상태 업데이트( main 메서드) <- AsyncCardApprovalService에서 호출
    @Override
    @Transactional
    public void updateChallengeProgressForNewTransactions() {
        try {
            log.info("====챌린지 업데이트 시작======");

            // 1. 마지막 스케쥴러 시간부터 현재까지의 거래 내역 조회(중복 방지)
            List<CardTransactionVO> newTransactions = findNewTransactionsToProcess();

            if (newTransactions.isEmpty()) {
                log.info("처리할 새로운 결제 내역이 없습니다.");
                return;
            }
            log.info("처리 대상 새로운 결제 내역: {} 건", newTransactions.size());

            // 2. 각 거래별로 관련 챌린지 참여자 처리
            int processedCount = 0;
            for (CardTransactionVO transaction : newTransactions) {
                try {
                    processSingleTransaction(transaction);
                    processedCount++;
                } catch (Exception e) {
                    log.error("거래 처리 실패 - 거래ID : {}", transaction.getId(), e);
                    // 개별 거래 실패가 전체를 중단 시키지 않도록 continue ~
                }
            }

            log.info("====챌린지 상태 업데이트 완료 - 처리:{} 건 / 전체 : {} 건 =====", processedCount, newTransactions.size());
        } catch (Exception e) {
            log.error("챌린지 상태 업데이트 중 전체 오류 발생", e);
            throw new RuntimeException("챌린지 상태 업데이트 실패", e);
        }
    }

    // 최근 생성된 거래 내역 조회
    @Override
    public List<CardTransactionVO> findNewTransactionsToProcess() {
        // 마지막 스케쥴러 실행 시간부터 현재까지의 거래만 조회
        String lastSchedulerTime = getLastSchedulerTime();
        String currentTime = LocalDateTime.now().toString();

        log.debug("거래조회범위 - 시작 : {}, 종료:{}", lastSchedulerTime, currentTime);
        return cardTransactionMapper.findTransactionBetweenTime(lastSchedulerTime, currentTime);
    }

    @Override
    public String getLastSchedulerTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDate currentDate = now.toLocalDate();

        LocalDateTime lastSchedulerTime;

        if (currentTime.isBefore(LocalTime.of(6, 0))) {
            // 현재 시간이 6시면 어제 0시
            lastSchedulerTime = currentDate.minusDays(1).atTime(0, 0);
        } else if (currentTime.isBefore(LocalTime.of(12, 0))) {
            // 현재 시간이 12시면 6시
            lastSchedulerTime = currentDate.atTime(6, 0);
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            // 현재 시간이 18시면 12시
            lastSchedulerTime = currentDate.atTime(12, 0);
        } else {
            // 현재 시간이 00시면 18시
            lastSchedulerTime = currentDate.atTime(18, 0);
        }
        return lastSchedulerTime.toString();
    }

    // 단일 거래에 대한 챌린지 처리
    @Override
    @Transactional
    public void processSingleTransaction(CardTransactionVO transaction) {
        try {
            log.debug("단일 거래 처리 시작 - 거래 ID: {}, 카테고리:{}", transaction.getId(), transaction.getCategoryId());

            // 취소된 거래는 처리 안함
            if ("Y".equals(transaction.getResCancelYn())) {
                log.debug("취소된 거래 스킵 - 거래ID :{} ", transaction.getId());
                return;
            }
            // 카테고리가 없는 거래는 처리하지 않음
            if (transaction.getCategoryId() == null) {
                log.debug("카테고리 미분류 거래 스킵 - 거래 id :{}", transaction.getId());
                return;
            }

            // 1. 해당 카테고리의 진행중인 챌린지 참여자들 조회
            List<ParticipationStatusDTO> activeParticipants = findActiveParticipantsByCategory(transaction.getCategoryId());

            if (activeParticipants.isEmpty()) {
                log.debug("해당 카테고리의 진행중인 참여자 없음 - 카테고리 :{} ", transaction.getCategoryId());
                return;
            }
            log.debug("해당 카테고리 진행중인 참여자: {} 명", activeParticipants.size());

            // 2. 각 참여자별로 진행상황 업데이트
            for (ParticipationStatusDTO participant : activeParticipants) {
                try {
                    processParticipantProgress(participant, transaction);
                } catch (Exception e) {
                    log.error("참여자 처리 실패 - 참여ID : {} ", participant.getParticipationId(), e);
                    // 개별 참여자 실패가 다른 참여자 처리를 중단시키지 않도록  continue
                }
            }
        } catch (Exception e) {
            log.error("단일 거래 처리 실패 - 거래ID: {}", transaction.getId(), e);
            throw e;
        }
    }


    //특정 카테고리의 진행중인 참여자 조회
    @Override
    public List<ParticipationStatusDTO> findActiveParticipantsByCategory(Long categoryId) {
        return challengeParticipationMapper.findActiveParticipantsByCategory(categoryId);
    }

    // 개별 참여자의 진행상황 처리
    @Override
    public void processParticipantProgress(ParticipationStatusDTO participant, CardTransactionVO transaction) {
        // 1. 업데이트 된 진행상호아 계신
        ChallengeProgressDTO progress = challengeProgressService.calculateUpdatedProgress(participant, transaction);

        // 2. 상태 업데이트 요청 생성
        ChallengeUpdateRequestDTO updateRequest = createUpdateRequest(progress);

        // 3. DB 업데이트 실행
        updateParticipationStatus(updateRequest);

        log.info("참여자 진행상황 업데이트 완료  - 참여ID: {}, 상태: {}, 누적({}타입): {}",
                participant.getParticipationId(),
                progress.getNewStatus(),
                progress.getType(),
                "COUNT".equals(progress.getType()) ? progress.getUpdatedCount() + "회" : progress.getUpdatedAmount() + "원");
    }


    // 업데이트 요청 DTO 생성
    @Override
    public ChallengeUpdateRequestDTO createUpdateRequest(ChallengeProgressDTO progress) {
        ChallengeUpdateRequestDTO.ChallengeUpdateRequestDTOBuilder builder = ChallengeUpdateRequestDTO.builder().participationId(progress.getParticipationId()).newStatus(progress.getNewStatus()).updatedCount(progress.getUpdatedCount()).updatedAmount(progress.getUpdatedAmount());

        // 실패 처리 시 완료 시간 설정
        if ("FAIL".equals(progress.getNewStatus())) {
            builder.completedAt(LocalDateTime.now());
        }
        return builder.build();
    }

    // 참여자 상태 업데이트 실행
    @Override
    public void updateParticipationStatus(ChallengeUpdateRequestDTO updateRequest) {
        if("FAIL".equals(updateRequest.getNewStatus())) {
            challengeParticipationMapper.updateStatusToFail(updateRequest);
        } else {
            challengeParticipationMapper.updateChallengeProgress(updateRequest.getParticipationId(), updateRequest.getUpdatedCount(),updateRequest.getUpdatedAmount());
        }

    }
}
