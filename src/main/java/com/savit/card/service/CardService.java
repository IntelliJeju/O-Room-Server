package com.savit.card.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savit.card.domain.Card;
import com.savit.card.dto.CardRegisterRequest;
import com.savit.card.mapper.CardMapper;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefTokenMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CodefTokenService codefTokenService;
    private final CodefUtil codefUtil;
    private final CardMapper cardMapper;

    public String registerAccount(CardRegisterRequest req) throws Exception {

        String accessToken = codefTokenService.getAccessToken();

        EasyCodefTokenMap.setToken(codefUtil.getClientId(), accessToken);

        EasyCodef client = codefUtil.newClient();

        HashMap<String, Object> account = new HashMap<>();
        account.put("countryCode", "KR");
        account.put("businessType", "CD");
        account.put("clientType", "P");
        account.put("organization", req.getOrganization());
        account.put("loginType", "1");
        account.put("loginTypeLevel", "2");
        account.put("id", req.getLoginId());
        account.put("password", req.getLoginPw());
        account.put("birthDate", req.getBirthDate());

        List<HashMap<String, Object>> list = List.of(account);
        HashMap<String, Object> params = new HashMap<>();
        params.put("accountList", list);

        String resp = client.createAccount(EasyCodefServiceType.DEMO, params);

        Map<?, ?> map = new ObjectMapper().readValue(resp, Map.class);
        Map<?, ?> data = (Map<?, ?>) map.get("data");
        String connectedId = (String) data.get("connectedId");

        if (connectedId == null) {
            throw new IllegalStateException("ConnectedId 발급 실패\n" + resp);
        }
        return connectedId;
    }

    public List<Map<String, Object>> fetchCardList(String connectedId,
                                                   String organization,
                                                   String birthDate) throws Exception {

        String accessToken = codefTokenService.getAccessToken();

        EasyCodefTokenMap.setToken(codefUtil.getClientId(), accessToken);

        EasyCodef client = codefUtil.newClient();

        HashMap<String, Object> params = new HashMap<>();
        params.put("connectedId", connectedId);
        params.put("organization", organization);
        params.put("birthDate", birthDate);
        params.put("inquiryType", "0");

        String resp = client.requestProduct(
                "/v1/kr/card/p/account/card-list",
                EasyCodefServiceType.DEMO,
                params
        );
        log.info("[CODEF] createAccount 응답 = {}", resp);


        Map<String, Object> map = new ObjectMapper().readValue(resp, Map.class);
        Object dataObj = map.get("data");

        List<Map<String, Object>> cardList;

        if (dataObj instanceof List) {
            cardList = (List<Map<String, Object>>) dataObj;
        } else if (dataObj instanceof Map) {
            cardList = List.of((Map<String, Object>) dataObj);
        } else {
            throw new IllegalStateException("예상치 못한 카드 응답 형식: " + dataObj);
        }

        return cardList;
    }

    public void saveCards(List<Map<String, Object>> cardDataList,
                          String connectedId,
                          String organization,
                          Long userId,
                          String encryptedCardNo,
                          String cardPassword) {

        List<Card> cards = cardDataList.stream().map(data -> Card.builder()
                        .connectedId(connectedId)
                        .organization(organization)
                        .cardName((String) data.get("resCardName"))
                        .issuer((String) data.get("issuer"))
                        .encryptedCardNo(encryptedCardNo)
                        .resCardNo((String) data.get("resCardNo"))
                        .resCardType((String) data.get("resCardType"))
                        .resSleepYn((String) data.get("resSleepYn"))
                        .cardPassword(cardPassword)
                        .registeredAt(LocalDateTime.now())
                        .userId(userId)
                        .build())
                .toList();

        cardMapper.insertCards(cards);
    }
}