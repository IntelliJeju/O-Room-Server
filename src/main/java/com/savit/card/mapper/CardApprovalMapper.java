package com.savit.card.mapper;

import com.savit.card.domain.CardApproval;
import com.savit.card.dto.ApprovalApiDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardApprovalMapper {

    /**
     * Codef API 호출에 필요한 데이터를 DB에서 조회
     * @param userId 사용자 ID
     * @param cardId 조회할 카드의 ID
     * @return API 호출에 필요한 데이터 DTO
     */
    ApprovalApiDataDTO findDataForApprovalApi(@Param("userId") Long userId, @Param("cardId") Long cardId);

    /**
     * 조회된 카드 승인 내역을 DB에 저장
     * @param approvals 저장할 승인 내역 리스트
     */
    void insertApprovals(@Param("approvals") List<CardApproval> approvals);

    /**
     * DB에 있는 승인내역 출력
     * @param userId 사용자 ID
     * @param cardId 조회할 카드 ID
     */
    List<CardApproval> findApprovalsByCardId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    /**
     * 사용자의 특정 월 모든 카드 승인내역 조회
     * @param userId 사용자 ID
     * @param month 조회할 월 (yyyyMM 형식)
     */
    List<CardApproval> findThisMonthApprovalsByUser(@Param("userId") Long
                                                            userId, @Param("month") String month);

    /**
     * 사용자 보유 카드 전체 id 값 가져오기
     */
    List<Long> findCardIdsByUser(Long userId);
}