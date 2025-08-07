package com.savit.challenge.service;

import com.savit.card.domain.CardTransactionVO;
import com.savit.card.mapper.CardTransactionMapper;
import com.savit.challenge.dto.ChallengeProgressDTO;
import com.savit.challenge.dto.ParticipationStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeProgressServiceImpl implements ChallengeProgressService{
    // 챌린지 진행상황 계산
    // 참여자별 누적 계산 및 목표 초과 여부


    private final CardTransactionMapper cardTransactionMapper;

    //  새로운 거래 반영한 챌린지 진행상황 계산
    @Override
    public ChallengeProgressDTO calculateUpdatedProgress(ParticipationStatusDTO participant, CardTransactionVO transaction) {
        try{
            log.debug("챌린지 진행상황 계산 시작 - 참여자 :{}, 거래: {}", participant.getParticipationId(), transaction.getId());

            // 1) 현재 누적값 가져오기
            Long currentCount = getCurrentCount(participant);
            BigDecimal currentAmount = getCurrentAmount(participant);

            // 2) 새로운 거래 반영
            Long updatedCount = currentCount;
            BigDecimal updatedAmount = currentAmount;

            if("COUNT".equals(participant.getType())) {
                updatedCount = currentCount + 1;
                log.debug("COUNT 타입 처리 - 기존: {} 회 , 업뎃:{} 회", currentCount,updatedCount);
            } else if ("AMOUNT".equals(participant.getType())) {
                BigDecimal transcationAmount = parseTransactionAmount(transaction.getResUsedAmount());
                updatedAmount = currentAmount.add(transcationAmount);
                log.debug("AMOUNT 타입 처리 - 기존 {} 원, 결제금액 {} 원, 업데이트 {} 원", currentAmount, transcationAmount, updatedAmount);

            }

            // 3) 목표 초과 여부 계산
            boolean isExceeded = calculateExceeded(participant, updatedCount, updatedAmount);
            String newStatus = isExceeded ? "FAIL" : "PARTICIPATING";

            // 4) 결과 Dto
            ChallengeProgressDTO result = ChallengeProgressDTO.builder()
                    .participationId(participant.getParticipationId())
                    .userId(participant.getUserId())
                    .challengeId(participant.getChallengeId())
                    .type(participant.getType())
                    .updatedCount(updatedCount)
                    .updatedAmount(updatedAmount)
                    .targetCount(participant.getTargetCount())
                    .targetAmount(participant.getTargetAmount())
                    .isExceeded(isExceeded)
                    .newStatus(newStatus).build();

            log.info("챌린지 진행상황 계산 완료 - 참여자: {}, 목표초과: {}, 새상태:{}", participant.getParticipationId(), isExceeded,newStatus);
            return result;
        } catch (Exception e ) {
            log.error("챌린지 진행상황 계산 실패 - 참여자 : {}", participant.getParticipationId(), e);
            throw  new RuntimeException("챌린지 진행상호아 계산 중 오류 발생", e);
        }
    }

    // 참여자의 challenge_Count 조회
    @Override
    public Long getCurrentCount(ParticipationStatusDTO participant) {
        return cardTransactionMapper.countByCategoryAndPeriod(
                participant.getUserId(),
                participant.getCategoryId(),
                participant.getStartDate().toString(),
                participant.getEndDate().toString()
        );
    }

    // 참여자의 challenge_Amount 조회
    @Override
    public BigDecimal getCurrentAmount(ParticipationStatusDTO participant) {
        BigDecimal amount =  cardTransactionMapper.sumAmountByCategoryAndPeriod(
                participant.getUserId(),
                participant.getCategoryId(),
                participant.getStartDate().toString(),
                participant.getEndDate().toString());
        return  amount != null ? amount : BigDecimal.ZERO;
    }

    // String 으로 들어오는 resUsedAmount BigDecimal로 파싱
    @Override
    public BigDecimal parseTransactionAmount(String resUsedAmount) {
        try {
            return new BigDecimal(resUsedAmount);
        } catch (NumberFormatException e) {
            log.warn("거래 금액 파싱 실패: {} . 0으로 처리", resUsedAmount);
            return BigDecimal.ZERO;
        }
    }

    // 목표 초과 여부 계산
    @Override
    public boolean calculateExceeded(ParticipationStatusDTO participant, Long updatedCount, BigDecimal updatedAmount) {
        if ("COUNT".equals(participant.getType())) {
            return updatedCount != null && participant.getTargetCount() != null && updatedCount >= participant.getTargetCount();
        } else if ("AMOUNT".equals(participant.getType())) {
            return updatedAmount != null && participant.getTargetAmount() != null && updatedAmount.compareTo(participant.getTargetAmount()) >= 0;

        } return  false;
    }
}
