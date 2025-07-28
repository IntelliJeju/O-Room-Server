package com.savit.card.controller;

import com.savit.card.domain.CardApproval;
import com.savit.card.dto.DashboardDTO;
import com.savit.card.service.CardApprovalService;
import com.savit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/{cardId}/approvals")
    public ResponseEntity<?> getCardApprovalHistory(
            @PathVariable Long cardId,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(request);
            List<CardApproval> approvals = cardApprovalService.getApprovalHistory(userId, cardId);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 메인 (대시보드)에 출력할 값 JSON 으로 반환됨
    // DashboardDTO 참고
    // 풀 url = /api/cards/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(request);
            DashboardDTO dashboard = cardApprovalService.getDashboardData(userId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("대시보드 조회 실패: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     *
     * @param request
     * @return 사용자 보유카드 전체 승인내역 한번에 조회(DB에 중복 제외 업데이트 가능)
     */
    @PostMapping("/approvals/all")
    public ResponseEntity<?> getAllCardApprovals(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(request);
            cardApprovalService.fetchAndSaveAllCards(userId);
            return ResponseEntity.ok(Map.of("message", "전체 카드 승인내역을 성공적으로 저장했습니다."));
        } catch (Exception e) {
            log.error("전체 승인내역 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}