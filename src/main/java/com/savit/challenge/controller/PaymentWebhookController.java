package com.savit.challenge.controller;

import com.savit.challenge.service.IamportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final IamportService iamportService;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        String impUid = (String) payload.get("imp_uid");

        log.info("========= [Webhook] 받은 imp_uid: {}", impUid);
        try {
            iamportService.processWebhook(impUid);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("======== [Webhook] 처리 실패", e);
            return ResponseEntity.status(500).body("fail");
        }
    }
}
