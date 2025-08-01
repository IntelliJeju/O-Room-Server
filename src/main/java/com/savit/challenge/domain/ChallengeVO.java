package com.savit.challenge.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ChallengeVO {
    private Long id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private BigDecimal entryFee;
    private int targetCount;
    private BigDecimal targetAmount;
    private String type;
    private int durationWeeks;
    private Long categoryId;
}
