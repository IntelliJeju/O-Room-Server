package com.savit.card.mapper;

import com.savit.card.domain.CardTransactionVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

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

    void insert(CardTransactionVO transaction);

    // 특정 시간 범위의 거래 내역 조회(중복 방지용)
    List<CardTransactionVO> findTransactionBetweenTime (@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 특정 시간 이후 생성된 거래 내역 조회(마지막 처리 시간 이후)
    List<CardTransactionVO> findTransactionAfterTime(String lastProcessedTime);

    // 특정 사용자의 특정 카테고리별 누적 금액 조회(챌린지 기간 내)
    BigDecimal sumAmountByCategoryAndPeriod(@Param("userId") Long userId,
                                            @Param("categoryId") Long categoryId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    // 횟수
    Long countByCategoryAndPeriod(@Param("userId") Long userId,
                                  @Param("categoryId") Long categoryId,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate);

}
