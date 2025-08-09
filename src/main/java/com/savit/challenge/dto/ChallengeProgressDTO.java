package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeProgressDTO {
    // 챌린지 진행상황 계산 결과 DTO
    // ChallengeProgressService 에서 계산한 진행상황 담는 용도
    private Long participationId;
    private Long userId;
    private Long challengeId;
    private String type;
    private Long updatedCount;
    private BigDecimal updatedAmount;
    private Long targetCount;
    private BigDecimal targetAmount;
    private boolean isExceeded; // target 초과 여부 확인
    private String newStatus;
}
