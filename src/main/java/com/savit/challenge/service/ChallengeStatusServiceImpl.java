package com.savit.challenge.service;

import com.savit.challenge.dto.ChallengeStatusDTO;
import com.savit.challenge.dto.ParticipantInfo;
import com.savit.challenge.mapper.ChallengeMapper;
import com.savit.challenge.mapper.ChallengeParticipationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeStatusServiceImpl implements ChallengeStatusService {

    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipationMapper participationMapper;

    @Override
    public ChallengeStatusDTO  getChallengeStatus(Long challengeId, Long userId) {
        // 1. 우선 메인 현황 정보들 가져오기
        ChallengeStatusDTO mainInfo = challengeMapper.selectChallengeStatus(challengeId, userId);

        // 2. challenge 타입 확인 후 참여자 목록 조회
        String type = challengeMapper.selectChallengeType(challengeId);

        List<ParticipantInfo> participants;
        if ("AMOUNT".equals(type)) {
            participants = participationMapper.selectParticipantsWithAmount(challengeId);
              mainInfo.setTargetCount(null);
        } else {
            participants = participationMapper.selectParticipantsWithCount(challengeId);
            mainInfo.setTargetAmount(null);
        }
        mainInfo.setParticipants(participants);

        return mainInfo;
    }


}