package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationStatusDTO {
    // 진행중인 챌린지 참여자 정보 DTO
    // challengeParticipationMapper에서 조회한 참여자 정보 담는 용도
    private Long participationId;
    private Long userId;
    private Long challengeId;
    private Long categoryId;
    private String type; // 챌린지 타입
    private String currentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long targetCount;
    private BigDecimal targetAmount;
    private Long currentCount;
    private BigDecimal currentAmount;

}
