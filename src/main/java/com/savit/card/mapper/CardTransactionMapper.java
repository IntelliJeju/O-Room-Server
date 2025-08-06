package com.savit.card.mapper;

import com.savit.card.domain.CardTransactionVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface CardTransactionMapper {

    void updateCategory(@Param("transactionId") Long transactionId,
                        @Param("categoryId") Long categoryId);

    List<CardTransactionVO> findUnclassifiedTransactionsByUser(@Param("userId") Long userId);

    Long findTransactionIdByCardIdAndDateTime(@Param("userId") Long userId,
                                              @Param("cardId") Long cardId,
                                              @Param("resUsedDate") String resUsedDate,
                                              @Param("resUsedTime") String resUsedTime);

    boolean isOwnedByUser(@Param("transactionId") Long transactionId,
                          @Param("userId") Long userId);


    // 챌린지 참여 조건 검사
    // 1) 금액 기준: type = AMOUNT
    BigDecimal sumAmountByParams (Map<String, Object> params);
    // 2) 횟수 기준: type = COUNT
    Long countByParams(Map<String, Object> params);

    // 전날 최고 지출 항목 조회 (사용자별, 카테고리명 포함)
    @MapKey("id") // 단일 결과여서 없어도 되는데 빨간 오류줄 없애려고 추가
    Map<String, Object> findTopSpendingByUserAndDate(@Param("userId") Long userId,
                                                     @Param("targetDate") String targetDate);
}
