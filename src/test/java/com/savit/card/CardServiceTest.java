package com.savit.card;

import com.savit.card.dto.CardRegisterRequest;
import com.savit.card.mapper.CardMapper;
import com.savit.card.service.CardService;
import com.savit.card.service.CodefTokenService;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CodefTokenService codefTokenService;
    @Mock private CodefUtil codefUtil;
    @Mock private CardMapper cardMapper;
    @Mock private EasyCodef codefClient;

    @InjectMocks
    private CardService cardService;

    @Test
    void registerAccount_정상응답_connectedId_반환() throws Exception {
        CardRegisterRequest req = new CardRegisterRequest();
        req.setLoginId("testId");
        req.setLoginPw("testPw");
        req.setBirthDate("19900101");
        req.setOrganization("0306");

        String connectedId = "mock-connected-id";
        String mockResp = "{\"data\":{\"connectedId\":\"" + connectedId + "\"}}";

        when(codefTokenService.getAccessToken()).thenReturn("token");
        when(codefUtil.getClientId()).thenReturn("client-id");
        when(codefUtil.encryptRSA(any())).thenReturn("encryptedPw");
        when(codefUtil.newClient()).thenReturn(codefClient);
        when(codefClient.createAccount(any(), any())).thenReturn(mockResp);

        String result = cardService.registerAccount(req);

        assertEquals(connectedId, result);
    }

    @Test
    void registerAccount_connectedId없을경우_예외발생() throws Exception {
        CardRegisterRequest req = new CardRegisterRequest();
        req.setLoginId("id");
        req.setLoginPw("pw");
        req.setBirthDate("19900101");
        req.setOrganization("0306");

        String mockResp = "{\"data\":{}}";

        when(codefTokenService.getAccessToken()).thenReturn("token");
        when(codefUtil.getClientId()).thenReturn("client-id");
        when(codefUtil.encryptRSA(any())).thenReturn("enc");
        when(codefUtil.newClient()).thenReturn(codefClient);
        when(codefClient.createAccount(any(), any())).thenReturn(mockResp);

        assertThrows(IllegalStateException.class, () -> cardService.registerAccount(req));
    }

    @Test
    void fetchCardList_정상응답() throws Exception {
        String connectedId = "cid";
        String organization = "0306";
        String birthDate = "19900101";

        String mockResp = "{\"data\":[{\"cardName\":\"신한카드\",\"resCardNo\":\"1234\"}]}";

        when(codefTokenService.getAccessToken()).thenReturn("token");
        when(codefUtil.getClientId()).thenReturn("client-id");
        when(codefUtil.newClient()).thenReturn(codefClient);
        when(codefClient.requestProduct(any(), any(), any())).thenReturn(mockResp);

        List<Map<String, Object>> result = cardService.fetchCardList(connectedId, organization, birthDate);

        assertFalse(result.isEmpty());
        assertEquals("신한카드", result.get(0).get("cardName"));
    }

    @Test
    void saveCards_cardMapper_호출확인() {
        Map<String, Object> data = new HashMap<>();
        data.put("cardName", "카드");
        data.put("issuer", "신한");
        data.put("resCardNo", "1234");
        data.put("resCardType", "체크");
        data.put("resSleepYn", "N");

        List<Map<String, Object>> list = List.of(data);

        cardService.saveCards(list, "cid", "org", 1L, "encNo", "00");

        verify(cardMapper).insertCards(anyList());
    }
}
