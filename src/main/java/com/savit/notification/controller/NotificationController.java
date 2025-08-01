package com.savit.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.savit.notification.dto.PushNotificationRequest;
import com.savit.notification.dto.FcmTokenRequest;
import com.savit.notification.service.NotificationService;
import com.savit.security.JwtUtil;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody PushNotificationRequest request) {
        try {
            String response = notificationService.sendNotification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 발송 실패", e);
            return ResponseEntity.internalServerError().body("알림 발송 실패: " + e.getMessage());
        }
    }

    /**
     * 프론트단에서 발급받은 FCM 토큰 받을 엔드포인트 -> DB에 토큰 저장까지
     * @param request
     * @param httpRequest
     * @return
     */
    @PostMapping("/token")
    public ResponseEntity<Void> saveFcmToken(
            @RequestBody FcmTokenRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(httpRequest);
            notificationService.saveUserFcmToken(userId, request.getFcmToken(), request.getDeviceType());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("FCM token 발급 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 유저에게 알림 발송 수동 테스트
     * (등록한 친구에게 알림발송용, 테스트 코드로 제대로 구현 X)
     * @param userId
     * @param title
     * @param body
     * @return
     */
    @PostMapping("/send-to-user/{userId}")
    public ResponseEntity<Void> sendNotificationToUser(
            @PathVariable Long userId,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            notificationService.sendNotificationToUser(userId, title, body);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("특정 유저ID에 알림 전송 실패 : {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}