package com.savit.challenge.service;

import com.savit.challenge.domain.ChallengeVO;
import com.savit.challenge.dto.ChallengeDetailDTO;
import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.challenge.dto.ChallengeSummaryDTO;

import java.util.List;

public interface ChallengeService {
    List<ChallengeListDTO> getChallengeList(Long userId);
    ChallengeDetailDTO getChallengeDetail(Long challengeId, Long userId);
    int checkEligibility(ChallengeVO vo, Long userId);

    List<ChallengeListDTO> getParticipatingChallenges (Long userId);

    List<ChallengeSummaryDTO> getChallengeSummary(Long userId);
}
