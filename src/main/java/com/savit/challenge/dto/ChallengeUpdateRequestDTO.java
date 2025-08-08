package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeUpdateRequestDTO {
    // 챌린지 참여자 상태 업데이트 요청
    private Long participationId;
    private String newStatus;
    private Long updatedCount;
    private BigDecimal updatedAmount;
    private LocalDateTime completedAt;

}
