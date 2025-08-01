package com.savit.notification.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChallengeNotificationDTO {
    private Long challengeId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}