package com.savit.card.mapper;

import com.savit.card.domain.CardTransactionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardTransactionMapper {

    // 카드 승인 내역 저장 (자동 또는 수동 분류 모두 사용)
    void insert(CardTransactionVO vo);

    // 수동 분류: 거래 내역의 카테고리 ID를 사용자가 선택한 값으로 갱신
    void updateCategory(@Param("transactionId") Long transactionId,
                        @Param("categoryId") Long categoryId,
                        @Param("resMemberStoreType") String resMemberStoreType);

    // 저장된 카테고리 ID NULL일 때 분류해서 저장
    List<CardTransactionVO> findUnclassifiedTransactions();
}
