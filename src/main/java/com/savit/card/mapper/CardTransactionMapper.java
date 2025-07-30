package com.savit.card.mapper;

import com.savit.card.domain.CardTransactionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardTransactionMapper {

    void updateCategory(@Param("transactionId") Long transactionId,
                        @Param("categoryId") Long categoryId,
                        @Param("resMemberStoreType") String resMemberStoreType);

    List<CardTransactionVO> findUnclassifiedTransactionsByUser(@Param("userId") Long userId);

    Long findTransactionIdByCardIdAndDateTime(@Param("userId") Long userId,
                                              @Param("cardId") Long cardId,
                                              @Param("resUsedDate") String resUsedDate,
                                              @Param("resUsedTime") String resUsedTime);

    boolean isOwnedByUser(@Param("transactionId") Long transactionId,
                          @Param("userId") Long userId);
}
