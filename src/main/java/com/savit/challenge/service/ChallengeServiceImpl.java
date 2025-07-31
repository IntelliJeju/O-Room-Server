package com.savit.challenge.service;

import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.challenge.mapper.ChallengeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChallengeServiceImpl implements ChallengeService{

    private final ChallengeMapper challengeMapper;

    @Override
    public List<ChallengeListDTO> getChallengeList(Long userId) {
            List<Long> categoryids = challengeMapper.findSuccessfulWeeklyCategories(userId);

            List<ChallengeListDTO> weeklyList = challengeMapper.findWeeklyChallenges();

            if(!categoryids.isEmpty()) {
                for (Long categoryid : categoryids) {
                    List<ChallengeListDTO> monthlyList = challengeMapper.findMonthlyChallenges(categoryid);
                    weeklyList.addAll(monthlyList);
                }
            }

            return weeklyList;
    }
}
