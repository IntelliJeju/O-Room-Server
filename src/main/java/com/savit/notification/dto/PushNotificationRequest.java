package com.savit.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {
    private String token;
    private String title;
    private String body;
    private String image;
    private Map<String, String> data;

    public PushNotificationRequest(String token, String title, String body) {
        this.token = token;
        this.title = title;
        this.body = body;
    }
}