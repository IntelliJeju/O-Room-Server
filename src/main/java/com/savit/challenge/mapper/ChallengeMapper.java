package com.savit.challenge.mapper;

import com.savit.challenge.dto.ChallengeListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChallengeMapper {
    List<Long> findSuccessfulWeeklyCategories(Long userId);
    List<ChallengeListDTO> findWeeklyChallenges();
    List<ChallengeListDTO> findMonthlyChallenges(Long categoryid);
}
