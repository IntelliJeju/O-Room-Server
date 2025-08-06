package com.savit.challenge.service;

import com.savit.challenge.dto.ChallengeStatusDTO;
import com.savit.challenge.mapper.ChallengeMapper;

import java.math.BigDecimal;
import java.util.List;

public interface ChallengeStatusService {
    ChallengeStatusDTO  getChallengeStatus(Long challengeId, Long userId);



}
