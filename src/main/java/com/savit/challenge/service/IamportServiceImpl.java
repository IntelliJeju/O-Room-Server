package com.savit.challenge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savit.challenge.dto.IamportPaymentResponseDTO;
import com.savit.challenge.mapper.ChallengeParticipationMapper;
import com.savit.challenge.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IamportServiceImpl implements IamportService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentMapper paymentMapper;
    private final ChallengeParticipationMapper participationMapper;

    @Value("${iamport.api-key}")
    private String apiKey;

    @Value("${iamport.api-secret}")
    private String apiSecret;

    @Override
    @Transactional
    public void processWebhook(String impUid) {
        String accessToken = fetchAccessToken();

        IamportPaymentResponseDTO payment = fetchPaymentInfo(impUid, accessToken);
        if (!"paid".equals(payment.getStatus())) {
            throw new IllegalStateException("결제 상태가 paid가 아님");
        }

        // 1. 결제 정보 저장
        paymentMapper.insertPayment(
                payment.getMerchantUid(),
                payment.getImpUid(),
                new Date(payment.getPaidAt() * 1000L),
                payment.getAmount(),
                "SUCCESS",
                payment.getChallengeId(),
                payment.getUserId()
        );

        // 2. 챌린지 참여 여부 확인
        boolean alreadyJoined = participationMapper.existsParticipation(
                payment.getChallengeId(),
                payment.getUserId()
        );

        // 3. 없을 경우 참여 확정 insert
        if (!alreadyJoined) {
            participationMapper.insertParticipation(
                    payment.getChallengeId(),
                    payment.getUserId(),
                    BigDecimal.valueOf(payment.getAmount())
            );
            log.info("챌린지 참여 등록 완료: challengeId={}, userId={}",
                    payment.getChallengeId(), payment.getUserId());
        } else {
            log.warn("이미 참여한 챌린지입니다: challengeId={}, userId={}",
                    payment.getChallengeId(), payment.getUserId());
        }

        log.info("결제 완료 처리: merchant_uid={}, userId={}, amount={}",
                payment.getMerchantUid(), payment.getUserId(), payment.getAmount());
    }

    private String fetchAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> payload = Map.of(
                "imp_key", apiKey,
                "imp_secret", apiSecret
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.iamport.kr/users/getToken", request, Map.class);

        return (String) ((Map) response.getBody().get("response")).get("access_token");
    }

    private IamportPaymentResponseDTO fetchPaymentInfo(String impUid, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + impUid,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("response");
        Map<String, Object> customData = parseCustomData(data.get("custom_data"));

        return new IamportPaymentResponseDTO(
                (String) data.get("imp_uid"),
                (String) data.get("merchant_uid"),
                ((Number) data.get("amount")).intValue(),
                (String) data.get("status"),
                ((Number) data.get("paid_at")).longValue(),
                ((Number) customData.get("challengeId")).longValue(),
                ((Number) customData.get("userId")).longValue()
        );
    }

    private Map<String, Object> parseCustomData(Object customDataRaw) {
        if (customDataRaw instanceof Map) {
            return (Map<String, Object>) customDataRaw;
        } else if (customDataRaw instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue((String) customDataRaw, Map.class);
            } catch (Exception e) {
                throw new RuntimeException("custom_data 문자열 파싱 실패", e);
            }
        }
        throw new RuntimeException("custom_data가 Map 또는 JSON 문자열이 아님");
    }

}