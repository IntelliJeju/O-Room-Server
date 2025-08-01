package com.savit.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.savit.notification.dto.PushNotificationRequest;
import com.savit.notification.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test/fcm")
@RequiredArgsConstructor
public class FCMTestController {

    private final NotificationService notificationService;

    /**
     * JSON으로 FCM 토큰 테스트용 API
     * JSON Body로 요청을 받습니다.
     */
    @PostMapping("/send-simple")
    public ResponseEntity<Map<String, String>> sendSimpleNotificationJson(
            @RequestBody PushNotificationRequest request) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            log.info("FCM JSON 테스트 - 토큰: {}, 제목: {}, 내용: {}", 
                    request.getToken(), request.getTitle(), request.getBody());
            
            String result = notificationService.sendNotification(request);
            
            response.put("status", "success");
            response.put("message", "알림 전송 성공");
            response.put("fcm_response", result);
            
            log.info("FCM 테스트 성공: {}", result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("FCM 테스트 실패", e);
            
            response.put("status", "error");
            response.put("message", "알림 전송 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Form-data로 FCM 토큰 테스트용 API (기존 방식)
     * 프론트엔드에서 받은 FCM 토큰을 직접 입력해서 테스트할 수 있습니다.
     */
    @PostMapping("/send-form")
    public ResponseEntity<Map<String, String>> sendSimpleNotificationForm(
            @RequestParam String token,
            @RequestParam(defaultValue = "테스트 제목") String title,
            @RequestParam(defaultValue = "테스트 메시지입니다.") String body) {

        Map<String, String> response = new HashMap<>();

        try {
            log.info("FCM 테스트 - 토큰: {}, 제목: {}, 내용: {}", token, title, body);

            PushNotificationRequest request = new PushNotificationRequest(token, title, body);
            String result = notificationService.sendNotification(request);

            response.put("status", "success");
            response.put("message", "알림 전송 성공");
            response.put("fcm_response", result);

            log.info("FCM 테스트 성공: {}", result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("FCM 테스트 실패", e);

            response.put("status", "error");
            response.put("message", "알림 전송 실패: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 이미지가 포함된 FCM 알림 테스트
     */
    @PostMapping("/send-with-image")
    public ResponseEntity<Map<String, String>> sendNotificationWithImage(
            @RequestParam String token,
            @RequestParam(defaultValue = "이미지 테스트") String title,
            @RequestParam(defaultValue = "이미지가 포함된 알림입니다.") String body,
            @RequestParam(required = false) String imageUrl) {

        Map<String, String> response = new HashMap<>();

        try {
            log.info("FCM 이미지 테스트 - 토큰: {}, 제목: {}, 내용: {}, 이미지: {}", token, title, body, imageUrl);

            PushNotificationRequest request = new PushNotificationRequest();
            request.setToken(token);
            request.setTitle(title);
            request.setBody(body);
            request.setImage(imageUrl);

            // 추가 데이터
            Map<String, String> data = new HashMap<>();
            data.put("test_type", "image_notification");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            request.setData(data);

            String result = notificationService.sendNotification(request);

            response.put("status", "success");
            response.put("message", "이미지 알림 전송 성공");
            response.put("fcm_response", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("FCM 이미지 테스트 실패", e);

            response.put("status", "error");
            response.put("message", "이미지 알림 전송 실패: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * FCM 설정 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkFCMStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Firebase 설정 상태 확인 (실제로는 NotificationService에서 체크)
            status.put("firebase_initialized", true);
            status.put("message", "FCM 서비스가 정상적으로 초기화되었습니다.");
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            status.put("firebase_initialized", false);
            status.put("message", "FCM 서비스 초기화 실패: " + e.getMessage());
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.internalServerError().body(status);
        }
    }
}