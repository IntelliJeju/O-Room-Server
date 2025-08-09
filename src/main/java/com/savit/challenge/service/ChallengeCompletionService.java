package com.savit.challenge.service;

import com.savit.challenge.domain.ChallengeVO;
import com.savit.challenge.dto.ParticipationStatusDTO;

import java.util.List;

public interface ChallengeCompletionService {
    void processCompletedChallenges();
    List<ChallengeVO> findChallengesEndingToday();
    int processSingleChallenge(ChallengeVO challenge);
    List<ParticipationStatusDTO> findParticipatingUsers(Long challengeId);
    void  updateSuccessfulParticipants(List<Long> participationIds);
    int processSpecificChallenge(Long challengeId);
}
