package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챌린지별 일일 낙오 요약 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDropoutSummaryDTO {
    private Long challengeId;           // 챌린지 ID
    private String challengeTitle;      // 챌린지 제목
    private Long totalParticipants;     // 전체 참여자 수
    private Long dropoutCount;          // 낙오자 수
    private Long activeCount;           // 활성 참여자 수 (낙오자 제외한 나머지)
    
}