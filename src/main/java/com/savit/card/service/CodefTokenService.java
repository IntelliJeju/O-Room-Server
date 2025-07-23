package com.savit.card.service;

import com.savit.card.domain.CodefToken;
import com.savit.card.repository.CodefTokenRepository;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefTokenService {

    private final CodefTokenRepository tokenRepository;
    private final CodefUtil codefUtil;

    @Transactional
    public String getAccessToken() {
        return tokenRepository.findValidToken()
                .map(CodefToken::getAccessToken)
                .orElseGet(this::issueAndSaveToken);
    }

    private String issueAndSaveToken() {

        try {
            EasyCodef client = codefUtil.newClient();

            String accessToken = client.requestToken(EasyCodefServiceType.DEMO);
            log.info("[CODEF] accessToken = '{}'", accessToken);

            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("CODEF 토큰 발급 실패 (빈 토큰)");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusWeeks(1);

            tokenRepository.save(CodefToken.builder()
                    .accessToken(accessToken)
                    .expiresAt(expiresAt)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());

            log.info("새 CODEF 토큰 저장완료 (만료 {} )", expiresAt);
            return accessToken;

        } catch (Exception e) {
            log.error("CODEF 토큰 발급 중 오류", e);
            throw new RuntimeException("CODEF 토큰 발급 실패", e);
        }
    }
}
