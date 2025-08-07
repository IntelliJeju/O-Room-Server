package com.savit.challenge.service;

import com.savit.card.domain.CardTransactionVO;
import com.savit.challenge.dto.ChallengeProgressDTO;
import com.savit.challenge.dto.ParticipationStatusDTO;

import java.math.BigDecimal;

public interface ChallengeProgressService {
    ChallengeProgressDTO calculateUpdatedProgress(ParticipationStatusDTO participant, CardTransactionVO transaction);
    Long getCurrentCount(ParticipationStatusDTO participant);
    BigDecimal getCurrentAmount (ParticipationStatusDTO participant);
    BigDecimal parseTransactionAmount(String resUsedAmount);
    boolean calculateExceeded(ParticipationStatusDTO participant, Long updatedCount, BigDecimal updatedAmount);
}
