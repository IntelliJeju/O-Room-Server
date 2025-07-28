package com.savit.card;

import com.savit.card.domain.CodefToken;
import com.savit.card.repository.CodefTokenRepository;
import com.savit.card.service.CodefTokenService;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodefTokenServiceTest {

    @Mock private CodefTokenRepository tokenRepository;
    @Mock private CodefUtil codefUtil;
    @Mock private EasyCodef codefClient;

    @InjectMocks
    private CodefTokenService tokenService;

    @Test
    void getAccessToken_유효토큰존재시_재사용() {
        CodefToken token = CodefToken.builder()
                .accessToken("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build();

        when(tokenRepository.findValidToken()).thenReturn(Optional.of(token));

        String result = tokenService.getAccessToken();

        assertEquals("valid-token", result);
    }

    @Test
    void getAccessToken_유효토큰없을때_신규발급후저장() throws Exception {
        when(tokenRepository.findValidToken()).thenReturn(Optional.empty());
        when(codefUtil.newClient()).thenReturn(codefClient);
        when(codefClient.requestToken(any())).thenReturn("new-token");

        String result = tokenService.getAccessToken();

        assertEquals("new-token", result);
        verify(tokenRepository).save(any(CodefToken.class));
    }

    @Test
    void issueAndSaveToken_빈토큰반환시_예외발생() throws Exception {
        when(tokenRepository.findValidToken()).thenReturn(Optional.empty());
        when(codefUtil.newClient()).thenReturn(codefClient);
        when(codefClient.requestToken(any())).thenReturn("");

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            tokenService.getAccessToken();
        });

        assertTrue(e.getMessage().contains("CODEF 토큰 발급 실패"));
        assertInstanceOf(IllegalStateException.class, e.getCause());
        assertEquals("CODEF 토큰 발급 실패 (빈 토큰)", e.getCause().getMessage());
    }

}
