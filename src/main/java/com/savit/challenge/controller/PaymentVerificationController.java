package com.savit.challenge.controller;

import com.savit.challenge.service.IamportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentVerificationController {

    private final IamportService iamportService;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyDirectly(@RequestBody Map<String, String> body) {
        String impUid = body.get("impUid");

        log.info("========= [Verify] 받은 imp_uid: {}", impUid);

        try {
            iamportService.processWebhook(impUid);
            return ResponseEntity.ok("verified");
        } catch (Exception e) {
            log.error("======== [Verify] 처리 실패", e);
            return ResponseEntity.status(500).body("fail");
        }
    }
}