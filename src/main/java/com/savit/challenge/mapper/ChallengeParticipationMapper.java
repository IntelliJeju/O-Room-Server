package com.savit.challenge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface ChallengeParticipationMapper {
    boolean existsParticipation(@Param("challengeId") Long challengeId,
                                @Param("userId") Long userId);

    void insertParticipation(@Param("challengeId") Long challengeId,
                             @Param("userId") Long userId,
                             @Param("myFee") BigDecimal myFee);

    // entry_fee 제거에 따라 challengeParticipation 테이블에서 my_fee 합치기
    BigDecimal sumMyFeeByChallenge(Long challengeId);
}
