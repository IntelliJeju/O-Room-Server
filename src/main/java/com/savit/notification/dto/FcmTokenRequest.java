package com.savit.notification.dto;

import lombok.Data;

@Data
public class FcmTokenRequest {
    private String fcmToken;
    private String deviceType;
}