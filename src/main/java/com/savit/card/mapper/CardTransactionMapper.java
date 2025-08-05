package com.savit.card.mapper;

import com.savit.card.domain.CardTransactionVO;
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
}
