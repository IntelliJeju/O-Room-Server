package com.savit.card.controller;

import com.savit.card.domain.CardApproval;
import com.savit.card.dto.CardTransactionDto;
import com.savit.card.dto.ManualCategoryRequest;
import com.savit.card.service.CardApprovalService;
import com.savit.card.service.CardTransactionService;
import com.savit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class CardTransactionController {

    private final CardTransactionService cardTransactionService;
    private final CardApprovalService cardApprovalService;
    private final JwtUtil jwtUtil;

    // 카드 승인 내역 저장 및 자동 분류
    @PostMapping
    public ResponseEntity<?> saveTransaction(@RequestBody CardTransactionDto dto) {
        cardTransactionService.save(dto);
        return ResponseEntity.ok("저장 완료");
    }

    // 사용자 수동 카테고리 지정
    @PutMapping("/{transactionId}/category")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long transactionId,
            @RequestBody ManualCategoryRequest req
    ) {
        req.setTransactionId(transactionId); // path variable → DTO에 주입
        cardTransactionService.updateCategory(req);
        return ResponseEntity.ok("카테고리 수동 분류 완료");
    }

    // 저장된 데이터 자동 재분류
    @PostMapping("/reclassify")
    public ResponseEntity<?> reclassifyUncategorized() {
        int updated = cardTransactionService.reclassifyUncategorizedTransactions();
        return ResponseEntity.ok(Map.of(
                "message", "자동 재분류 완료",
                "updatedCount", updated
        ));
    }
}
