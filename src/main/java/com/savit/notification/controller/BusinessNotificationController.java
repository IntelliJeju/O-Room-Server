package com.savit.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.savit.notification.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/business/notifications")
@RequiredArgsConstructor
public class BusinessNotificationController {
    private final NotificationService notificationService;
    
    /**
     * 예산 초과 알림 테스트
     */
    @PostMapping("/budget-exceeded/{userId}")
    public ResponseEntity<Map<String, String>> sendBudgetExceededTest(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50,000원") String exceededAmount,
            @RequestParam(defaultValue = "300,000원") String totalBudget) {
        
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendBudgetExceededNotification(userId, exceededAmount, totalBudget);
            response.put("status", "success");
            response.put("message", "예산 초과 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 카테고리 예산 경고 알림 테스트
     */
    @PostMapping("/budget-warning/{userId}")
    public ResponseEntity<Map<String, String>> sendBudgetWarningTest(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "식비") String categoryName,
            @RequestParam(defaultValue = "80") int usagePercent,
            @RequestParam(defaultValue = "50,000원") String remainingAmount) {
        
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendCategoryBudgetWarning(userId, categoryName, usagePercent, remainingAmount);
            response.put("status", "success");
            response.put("message", "카테고리 예산 경고 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 카드 사용 알림 테스트
     * 승인내역은 카드사 자체 SMS 또는 앱푸시로 보게끔
     * 단순 테스트용으로 생성, 실제 운영시 사용 X
     */
    @PostMapping("/card-usage/{userId}")
    public ResponseEntity<Map<String, String>> sendCardUsageTest(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "스타벅스") String storeName,
            @RequestParam(defaultValue = "5,500원") String amount,
            @RequestParam(defaultValue = "카페/음료") String storeType) {
        
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendCardUsageNotification(userId, storeName, amount, storeType);
            response.put("status", "success");
            response.put("message", "카드 사용 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 랜덤 잔소리 알림 테스트
     */
    @PostMapping("/random-nagging/{userId}")
    public ResponseEntity<Map<String, String>> sendRandomNaggingTest(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendRandomNaggingNotification(userId);
            response.put("status", "success");
            response.put("message", "랜덤 잔소리 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 챌린지 성공 알림 테스트 - 임시용 생성, 이렇게 안쓸듯..
     */
    @PostMapping("/challenge-success/{userId}")
    public ResponseEntity<Map<String, String>> sendChallengeSuccessTest(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30일 용돈기입장 쓰기") String challengeTitle,
            @RequestParam(defaultValue = "10,000원") String prize) {
        
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendChallengeSuccessNotification(userId, challengeTitle, prize);
            response.put("status", "success");
            response.put("message", "챌린지 성공 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 챌린지 실패 알림 테스트 - 임시용 생성, 이렇게 안쓸듯..
     */
    @PostMapping("/challenge-fail/{userId}")
    public ResponseEntity<Map<String, String>> sendChallengeFailTest(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30일 용돈기입장 쓰기") String challengeTitle) {
        
        Map<String, String> response = new HashMap<>();
        try {
            notificationService.sendChallengeFailNotification(userId, challengeTitle);
            response.put("status", "success");
            response.put("message", "챌린지 실패 알림 전송 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}