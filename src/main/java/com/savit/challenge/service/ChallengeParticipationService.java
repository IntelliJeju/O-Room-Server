package com.savit.challenge.service;

import com.savit.card.domain.CardTransactionVO;
import com.savit.challenge.dto.ChallengeProgressDTO;
import com.savit.challenge.dto.ChallengeUpdateRequestDTO;
import com.savit.challenge.dto.ParticipationStatusDTO;

import java.util.List;

public interface ChallengeParticipationService {
    void updateChallengeProgressForNewTransactions();
    List<CardTransactionVO> findNewTransactionsToProcess();
    void processSingleTransaction(CardTransactionVO transaction);
    String getLastSchedulerTime();
    List<ParticipationStatusDTO> findActiveParticipantsByCategory(Long categoryId);
    void processParticipantProgress(ParticipationStatusDTO participant, CardTransactionVO transaction);
    ChallengeUpdateRequestDTO createUpdateRequest(ChallengeProgressDTO progress);
    void updateParticipationStatus(ChallengeUpdateRequestDTO updateRequest);




}
