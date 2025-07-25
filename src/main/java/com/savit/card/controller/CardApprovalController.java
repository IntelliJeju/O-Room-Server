package com.savit.card.controller;

import com.savit.card.domain.CardApproval;
import com.savit.card.service.CardApprovalService;
import com.savit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardApprovalController {

    private final CardApprovalService cardApprovalService;
    private final JwtUtil jwtUtil;

    /**
     * 특정 카드의 승인 내역을 조회하고 DB에 저장하는 API
     * @param cardId 카드 ID (PK)
     * @param request HttpServletRequest
     * @return 저장된 승인 내역 리스트
     */
    @PostMapping("/{cardId}/approvals")
    public ResponseEntity<?> getCardApprovals(
            @PathVariable Long cardId,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(request);
            List<CardApproval> approvals = cardApprovalService.fetchAndSaveApprovals(userId, cardId);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}