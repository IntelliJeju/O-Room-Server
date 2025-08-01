package com.savit.challenge.mapper;

import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.notification.dto.ChallengeNotificationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChallengeMapper {
    List<ChallengeListDTO> getChallengeList();

    // 시작일 기준
    List<ChallengeNotificationDTO> findByStartDate(@Param("startDate") LocalDate startDate);

    // 종료일 기준
    List<ChallengeNotificationDTO> findByEndDate(@Param("endDate") LocalDate endDate);

    // 챌린지 ID로 참여 중인 유저 ID 리스트 조회
    List<Long> findUserIdsByChallengeId(@Param("challengeId") Long challengeId);
}
