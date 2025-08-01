package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeDetailDTO {
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private BigDecimal entryFee;
    private String categoryName;
    private int eligibility;
}
