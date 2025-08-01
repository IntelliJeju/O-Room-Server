package com.savit.challenge.mapper;

import com.savit.challenge.domain.ChallengeVO;
import com.savit.challenge.dto.ChallengeListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChallengeMapper {
    // 리스트 조회
    List<Long> findSuccessfulWeeklyCategories(Long userId);
    List<ChallengeListDTO> findWeeklyChallenges();
    List<ChallengeListDTO> findMonthlyChallenges(Long categoryid);

    // 상세조회
    ChallengeVO findById(Long id);

    // 참여중인 챌린지 조회
    List<ChallengeListDTO> findParticipatingChallenges(Long userId);
}
