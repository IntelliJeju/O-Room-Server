package com.savit.card.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savit.card.domain.CardApproval;
import com.savit.card.dto.ApprovalApiDataDTO;
import com.savit.card.mapper.CardApprovalMapper;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefTokenMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardApprovalService {

    private final CardApprovalMapper cardApprovalMapper;
    private final CodefTokenService codefTokenService;
    private final CodefUtil codefUtil;
//    private final ObjectMapper objectMapper;

    @Transactional
    public List<CardApproval> fetchAndSaveApprovals(Long userId, Long cardId) throws Exception {
        log.info("=== 승인내역 조회 시작 - userId: {}, cardId: {} ===", userId, cardId);
        
        // 1. DB에서 API 호출에 필요한 데이터 조회
        log.info("1. DB 데이터 조회 시작");
        ApprovalApiDataDTO apiData = cardApprovalMapper.findDataForApprovalApi(userId, cardId);
        if (apiData == null) {
            throw new IllegalStateException("카드 정보 또는 사용자 정보를 찾을 수 없습니다.");
        }
        log.info("DB 데이터 조회 완료 - resCardNo: {}", apiData.getResCardNo());

        // 2. Codef API 호출
        log.info("2. Codef API 호출 시작");
        List<Map<String, Object>> approvalDataList = callApprovalHistoryApi(apiData);
        log.info("Codef API 호출 완료 - 조회된 승인내역 수: {}", approvalDataList.size());

        // 3. 응답 결과를 CardApproval 도메인 객체로 변환
        List<CardApproval> approvals = approvalDataList.stream()
                .map(data -> CardApproval.builder()
                        .cardId(cardId)
                        .budgetCategoryId(null) // Foreign Key 제약조건 회피를 위해 임시로 NULL 설정
                        .categoryId(null) // TODO: 가맹점 업종에 따른 카테고리 매핑 로직 필요
                        .resCardNo((String) data.get("resCardNo"))
                        .resUsedDate((String) data.get("resUsedDate"))
                        .resUsedTime((String) data.get("resUsedTime"))
                        .resUsedAmount((String) data.get("resUsedAmount"))
                        .resCancelYN((String) data.get("resCancelYN"))
                        .resCancelAmount((String) data.get("resCancelAmount"))
                        .resTotalAmount((String) data.get("resTotalAmount"))
                        .resMemberStoreName((String) data.get("resMemberStoreName"))
                        .resMemberStoreType((String) data.get("resMemberStoreType"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        // 4. 변환된 승인 내역을 DB에 저장
        if (!approvals.isEmpty()) {
            cardApprovalMapper.insertApprovals(approvals);
        }

        return approvals;
    }

    // 승인내역 API 호출
    private List<Map<String, Object>> callApprovalHistoryApi(ApprovalApiDataDTO apiData) throws Exception {
        // Codef 토큰 설정
        String accessToken = codefTokenService.getAccessToken();
        EasyCodefTokenMap.setToken(codefUtil.getClientId(), accessToken);
        EasyCodef client = codefUtil.newClient();

        // 조회 기간 설정 (1개월전부터 오늘까지) - 성능 향상을 위해 단축
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // API 파라미터 설정
        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", apiData.getOrganization());
        params.put("connectedId", apiData.getConnectedId());
        params.put("birthDate", apiData.getBirthDate());
        params.put("startDate", (String)start.format(formatter));
        params.put("endDate", end.format(formatter));
        params.put("orderBy", "0"); // 최신순(0)
        params.put("inquiryType", "0"); // 조회구분(0:카드별 조회)
        params.put("cardName", apiData.getCardName()); // 실제 카드명 사용
        params.put("cardNo", apiData.getResCardNo()); // 마스킹된 번호 사용
        params.put("cardPassword", apiData.getCardPassword()); // 암호화된 값 그대로 사용
        params.put("memberStoreInfoType", "1"); // 가맹점 정보 포함 "1"
        
        log.info("API 파라미터 - cardNo: {}, cardName: {}", apiData.getResCardNo(), apiData.getCardName());

        String resp = client.requestProduct(
                "/v1/kr/card/p/account/approval-list",
                EasyCodefServiceType.DEMO,
                params
        );
        log.info("[CODEF] /v1/kr/card/p/account/approval-list 응답 = {}", resp);

        Map<?, ?> map = new ObjectMapper().readValue(resp, Map.class);
        Object dataObj = map.get("data"); // 응답 JSON 에서 승인내역들
        List<Map<String, Object>> cardApprovalList;

        if (map.get("result") != null && "CF-00000".equals(((Map<?, ?>) map.get("result")).get("code"))) {
            if (dataObj instanceof List) {
                cardApprovalList = (List<Map<String, Object>>) dataObj;
            } else if (dataObj instanceof Map) {
                cardApprovalList = List.of((Map<String, Object>) dataObj);
            } else {
                throw new IllegalStateException("예상치 못한 카드 승인내역 응답 형식: " + dataObj);
            }
            return cardApprovalList;
        } else {
            log.error("Codef API 호출 실패: {}", resp);
            throw new IllegalStateException("카드 승인내역 조회에 실패했습니다. 응답: " + resp);
        }
    }
}