package com.savit.notification.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PushNotification {
    private Long id;
    private Long userId;
    private String fcmToken;
    private String title;
    private String body;
    private String type;
    private String status; // PENDING, SENT, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String errorMessage;
}