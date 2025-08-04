package com.savit.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.savit.notification.dto.PushNotificationRequest;
import com.savit.notification.domain.PushNotification;
import com.savit.notification.domain.UserFcmToken;
import com.savit.notification.mapper.NotificationMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationMapper notificationMapper;

    public String sendNotification(PushNotificationRequest request) {
        return sendNotification(request, null);
    }

    public String sendNotification(PushNotificationRequest request, Long userId) {
        if (firebaseMessaging == null) {
            log.error("FirebaseMessaging is not initialized");
            throw new RuntimeException("Firebase messaging service is not available");
        }

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .setImage(request.getImage())
                .build();

        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(notification)
                .putAllData(request.getData() != null ? request.getData() : java.util.Collections.emptyMap())
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .setIcon("/icon-192x192.png")
                                .build())
                        .build())
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("Successfully sent message: {}", response);

            PushNotification pushNotification = PushNotification.builder()
                    .userId(userId)
                    .fcmToken(request.getToken())
                    .title(request.getTitle())
                    .body(request.getBody())
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationMapper.insertNotification(pushNotification);

            return response;
        } catch (FirebaseMessagingException e) { // 에러 확인용으로 DB에 일단 저장
            log.error("Failed to send message: {}", e.getMessage());

            PushNotification pushNotification = PushNotification.builder()
                    .userId(userId)
                    .fcmToken(request.getToken())
                    .title(request.getTitle())
                    .body(request.getBody())
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationMapper.insertNotification(pushNotification);

            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public void saveUserFcmToken(Long userId, String fcmToken, String deviceType) {
        notificationMapper.deactivateUserTokens(userId, deviceType);
        notificationMapper.insertUserFcmToken(userId, fcmToken, deviceType);
        log.info("FCM token saved for user: {} with device type: {}", userId, deviceType);
    }

    public List<UserFcmToken> getActiveTokensByUserId(Long userId) {
        return notificationMapper.findActiveTokensByUserId(userId);
    }

    public void sendNotificationToUser(Long userId, String title, String body) {
        List<UserFcmToken> tokens = getActiveTokensByUserId(userId);

        for (UserFcmToken token : tokens) {
            PushNotificationRequest request = new PushNotificationRequest(
                    token.getFcmToken(), title, body
            );
            try {
                sendNotification(request, userId);
            } catch (Exception e) {
                log.error("Failed to send notification to token: {}", token.getFcmToken(), e);
            }
        }
    }
    
    // 비즈니스 로직 기반 알림 전송 메서드들
    
    /**
     * 예산 초과 알림
     */
    public void sendBudgetExceededNotification(Long userId, String exceededAmount, String totalBudget) {
        String title = "💸 예산 초과 알림";
        String body = String.format("이번 달 예산을 %s 초과했어요! (예산: %s)", exceededAmount, totalBudget);
        sendNotificationToUser(userId, title, body);
        log.info("예산 초과 알림 전송 완료 - 사용자: {}, 초과금액: {}", userId, exceededAmount);
    }
    
    /**
     * 카테고리별 예산 경고 (80% 사용시)
     */
    public void sendCategoryBudgetWarning(Long userId, String categoryName, int usagePercent, String remainingAmount) {
        String title = "⚠️ 예산 사용 경고";
        String body = String.format("%s 예산의 %d%% 사용! %s만 남았어요!", categoryName, usagePercent, remainingAmount);
        sendNotificationToUser(userId, title, body);
        log.info("카테고리 예산 경고 전송 완료 - 사용자: {}, 카테고리: {}, 사용률: {}%", userId, categoryName, usagePercent);
    }
    
    /**
     * 카드 사용 알림
     */
    public void sendCardUsageNotification(Long userId, String storeName, String amount, String storeType) {
        String title = "💳 카드 사용 알림";
        String body = String.format("%s에서 %s 사용 (%s)", 
                storeName != null ? storeName : "가맹점", 
                amount, 
                storeType != null ? storeType : "");
        sendNotificationToUser(userId, title, body);
        log.info("카드 사용 알림 전송 완료 - 사용자: {}, 금액: {}, 가맹점: {}", userId, amount, storeName);
    }
    
    /**
     * 랜덤 잔소리 알림
     */
    public void sendRandomNaggingNotification(Long userId) {
        String[] naggingMessages = {
            "또 신용카드 긁기만 해봐 💸",
            "돈 관리 좀 제대로 해보자! 💰",
            "잔여 예산 확인은 언제 할 거야? 📊",
            "용돈 기입장이라도 써봐! 📝",
            "카드 명세서 보면 깜짝 놀랄걸? 😱",
            "절약 좀 해보자구요~",
            "이번 달 예산 벌써 다 썼어? 😤",
            "신용카드 또 긁었어? 아니지?"
        };
        
        String naggingMessage = naggingMessages[(int) (Math.random() * naggingMessages.length)];
        String title = "💬 Savit 한마디";
        sendNotificationToUser(userId, title, naggingMessage);
        log.info("랜덤 잔소리 알림 전송 완료 - 사용자: {}, 메시지: {}", userId, naggingMessage);
    }
    
    /**
     * 챌린지 성공 알림 - 임시용 생성, 이렇게 안쓸듯..
     */
    public void sendChallengeSuccessNotification(Long userId, String challengeTitle, String prize) {
        String title = "🎉 챌린지 성공!";
        String body = String.format("'%s' 챌린지를 성공했어요! 상금: %s", challengeTitle, prize);
        sendNotificationToUser(userId, title, body);
        log.info("챌린지 성공 알림 전송 완료 - 사용자: {}, 챌린지: {}", userId, challengeTitle);
    }
    
    /**
     * 챌린지 실패 알림 - 임시용 생성, 이렇게 안쓸듯..
     */
    public void sendChallengeFailNotification(Long userId, String challengeTitle) {
        String title = "😢 챌린지 실패";
        String body = String.format("'%s' 챌린지에 실패했어요. 다음에 더 열심히 해봐요!", challengeTitle);
        sendNotificationToUser(userId, title, body);
        log.info("챌린지 실패 알림 전송 완료 - 사용자: {}, 챌린지: {}", userId, challengeTitle);
    }
}