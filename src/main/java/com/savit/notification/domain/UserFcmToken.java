package com.savit.notification.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFcmToken {
    private Long id;
    private Long userId;
    private String fcmToken;
    private String deviceType; // WEB, ANDROID, IOS
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}