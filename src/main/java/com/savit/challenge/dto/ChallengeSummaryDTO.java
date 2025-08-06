package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeSummaryDTO {
    private Long challengeId;
    private String title;
    private String startDate;
    private String endDate;
    private String status;
    private BigDecimal my_fee;
    private BigDecimal prize;
}
