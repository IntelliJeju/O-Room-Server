package com.savit.card.service;

import com.savit.budget.domain.CategoryVO;
import com.savit.budget.mapper.CategoryMapper;
import com.savit.card.domain.CardTransactionVO;
import com.savit.card.dto.CardTransactionDto;
import com.savit.card.dto.ManualCategoryRequest;
import com.savit.card.mapper.CardTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardTransactionService {

    private final CardTransactionMapper cardTransactionMapper;
    private final CategoryMapper categoryMapper;

    private static final Map<String, List<String>> NAME_KEYWORDS = Map.of(
            "식비", List.of("배달의민족", "요기요", "맘스터치", "김밥", "한솥"),
            "카페/간식", List.of("스타벅스", "이디야", "컴포즈", "투썸", "커피", "베스킨"),
            "교통", List.of("택시", "온다", "버스", "지하철", "티머니"),
            "패션/쇼핑", List.of("쿠팡", "11번가", "g마켓", "네이버파이낸셜"),
            "생활", List.of("GS25", "CU", "세븐일레븐", "이마트24", "다이소"),
            "유흥", List.of("술집", "호프", "포차", "맥주", "노래방"),
            "문화/여가", List.of("CGV", "메가박스", "롯데시네마", "예술의전당", "인터파크")
    );

    private static final Map<String, String> SAFE_STORE_TYPE_MAPPING = Map.of(
            "택시", "교통",
            "버스", "교통",
            "지하철", "교통",
            "편의점", "생활"
    );

    // 카드 승인 내역 저장 후 자동 분류
    public void save(CardTransactionDto dto) {
        CategoryVO category = classifyCategory(dto.getResMemberStoreName(), dto.getResMemberStoreType());

        CardTransactionVO vo = buildTransaction(dto);
        vo.setCategoryId(category.getId());
        vo.setBudgetCategoryId(category.getId());
        vo.setResMemberStoreType(category.getName());

        cardTransactionMapper.insert(vo);
    }

    // 수동 카테고리 지정
    public void updateCategory(ManualCategoryRequest req) {
        Long categoryId = req.getCategoryId();
        CategoryVO category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("해당 카테고리 ID를 찾을 수 없습니다: " + categoryId);
        }

        cardTransactionMapper.updateCategory(req.getTransactionId(), categoryId, category.getName());
    }

    // 자동 재분류
    public int reclassifyUncategorizedTransactions() {
        List<CardTransactionVO> transactions = cardTransactionMapper.findUnclassifiedTransactions();
        int updatedCount = 0;

        for (CardTransactionVO tx : transactions) {
            try {
                CategoryVO category = classifyCategory(tx.getResMemberStoreName(), tx.getResMemberStoreType());
                cardTransactionMapper.updateCategory(tx.getId(), category.getId(), category.getName());
                updatedCount++;
            } catch (Exception e) {
                log.warn("자동 분류 실패 - txId: {}, error: {}", tx.getId(), e.getMessage());
            }
        }

        return updatedCount;
    }

    // 분류 로직
    private CategoryVO classifyCategory(String storeName, String storeType) {
        if (storeName != null && !storeName.isBlank()) {
            String normalized = normalize(storeName);
            for (Map.Entry<String, List<String>> entry : NAME_KEYWORDS.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (normalized.contains(keyword.toLowerCase())) {
                        return getCategoryOrThrow(entry.getKey());
                    }
                }
            }
        }

        if (storeType != null && !storeType.isBlank()) {
            for (Map.Entry<String, String> entry : SAFE_STORE_TYPE_MAPPING.entrySet()) {
                if (storeType.contains(entry.getKey())) {
                    return getCategoryOrThrow(entry.getValue());
                }
            }
        }

        return getCategoryOrThrow("기타");
    }

    // 카테고리명으로 조회 실패 시 예외
    private CategoryVO getCategoryOrThrow(String name) {
        CategoryVO category = categoryMapper.findByName(name);
        if (category == null) {
            throw new IllegalStateException("'" + name + "' 카테고리가 DB에 존재하지 않습니다.");
        }
        return category;
    }

    // 상호명 전처리
    private String normalize(String text) {
        return text.replaceAll("[^가-힣a-zA-Z0-9]", "").toLowerCase();
    }

    // DTO → VO 변환
    private CardTransactionVO buildTransaction(CardTransactionDto dto) {
        CardTransactionVO vo = new CardTransactionVO();
        vo.setCardId(dto.getCardId());
        vo.setResMemberStoreName(dto.getResMemberStoreName());
        vo.setResUsedAmount(dto.getResUsedAmount());
        vo.setResUsedDate(dto.getResUsedDate());
        vo.setResUsedTime(dto.getResUsedTime());
        vo.setResMemberStoreType(dto.getResMemberStoreType());
        return vo;
    }
}
