package com.savit.challenge.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeStatusDTO {
    private String title;
    private Long joinedParticipants;
    private Long participatingParticipants;
    private BigDecimal entryFee;
    private BigDecimal myFee;
    private String startDate;
    private String endDate;
    private BigDecimal expectedPrize;
    private List<ParticipantInfo> participants;

    private BigDecimal targetAmount;
    private Long targetCount;
}
