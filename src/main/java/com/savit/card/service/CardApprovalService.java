package com.savit.card.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savit.card.domain.CardApproval;
import com.savit.card.dto.ApprovalApiDataDTO;
import com.savit.card.dto.BudgetMonitoringDTO;
import com.savit.card.dto.DashboardDTO;
import com.savit.card.mapper.CardApprovalMapper;
import com.savit.budget.service.BudgetService;
import com.savit.budget.domain.BudgetVO;
import com.savit.card.util.CodefUtil;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefTokenMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardApprovalService {

    private final CardApprovalMapper cardApprovalMapper;
    private final CodefTokenService codefTokenService;
    private final CodefUtil codefUtil;
    private final BudgetService budgetService;
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

//
        // 3. 기존 승인내역 조회
        List<CardApproval> existingApprovals = cardApprovalMapper.findApprovalsByCardId(userId, cardId);

        // 4. 기존 승인내역을 식별 가능한 키로 Set에 저장
        Set<String> existingKeys = existingApprovals.stream()
                .map(this::generateUniqueKey)
                .collect(Collectors.toSet());

        // 5. 새로 가져온 승인 데이터 중 기존에 없는 것만 필터링
        log.info("기존 승인내역 수: {}, 고유 키 수: {}",
                existingApprovals.size(), existingKeys.size());

        // 5. 새로 가져온 승인 데이터 처리
        List<CardApproval> newApprovals = approvalDataList.stream()
                .map(data -> {
                    CardApproval approval = CardApproval.builder()
                            .cardId(cardId)
                            .budgetCategoryId(null)
                            .categoryId(null)
                            .resCardNo((String) data.get("resCardNo"))
                            .resUsedDate((String) data.get("resUsedDate"))
                            .resUsedTime((String) data.get("resUsedTime"))
                            .resUsedAmount((String)
                                    data.get("resUsedAmount"))
                            .resCancelYN((String) data.get("resCancelYN"))
                            .resCancelAmount((String)
                                    data.get("resCancelAmount"))
                            .resTotalAmount((String)
                                    data.get("resTotalAmount"))
                            .resMemberStoreName((String)
                                    data.get("resMemberStoreName"))
                            .resMemberStoreType((String)
                                    data.get("resMemberStoreType"))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    log.debug("처리 중인 승인내역 키: {}",
                            generateUniqueKey(approval));
                    return approval;
                })
                .filter(approval -> {
                    String key = generateUniqueKey(approval);
                    boolean isNew = !existingKeys.contains(key);
                    if (!isNew) {
                        log.debug("중복 건너뛰기: {}", key);
                    }
                    return isNew;
                })
                .collect(Collectors.toList());

        log.info("새로 저장할 승인내역 수: {}", newApprovals.size());

        // 6. 새로운 승인내역만 저장
        if (!newApprovals.isEmpty()) {
            cardApprovalMapper.insertApprovals(newApprovals);
            log.info("새 승인내역 저장 완료: {}건", newApprovals.size());
        } else {
            log.info("저장할 새 승인내역이 없습니다.");
        }

        return newApprovals;
    }

    public void fetchAndSaveAllCards(Long userId) {
        List<Long> cardIds = cardApprovalMapper.findCardIdsByUser(userId); // 사용자 카드 전체 조회
        for (Long cardId : cardIds) {
            try {
                fetchAndSaveApprovals(userId, cardId);
            } catch (Exception e) {
                log.error("카드 {} 처리 중 오류", cardId, e);
            }
        }
    }

    // 안전한 고유 키 생성 메서드
    private String generateUniqueKey(CardApproval approval) {
        return String.format("%s_%s_%s_%s_%s",
                safeString(approval.getResCardNo()),
                safeString(approval.getResUsedDate()),
                safeString(approval.getResUsedTime()),
                safeString(approval.getResUsedAmount()),
                safeString(approval.getResMemberStoreName()) // 가맹점명으로 더 정확한 식별
        );
    }

    // null 안전 문자열 변환
    private String safeString(String value) {
        return value != null ? value : "NULL";
    }

    // 승인내역 API 호출
    private List<Map<String, Object>> callApprovalHistoryApi(ApprovalApiDataDTO apiData) throws Exception {
        // Codef 토큰 설정
        String accessToken = codefTokenService.getAccessToken();
        EasyCodefTokenMap.setToken(codefUtil.getClientId(), accessToken);
        EasyCodef client = codefUtil.newClient();

        // 조회 기간 설정 (1개월전부터 오늘까지) - 성능 향상을 위해 단축
        LocalDate end = LocalDate.now();
        // 이전달 1일 부터 가져오게끔 수정
        LocalDate start = end.minusMonths(1).withDayOfMonth(1);
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

    // DB에 저장된 승인내역 보기
    public List<CardApproval> getApprovalHistory(Long userId, Long cardId) {
        return cardApprovalMapper.findApprovalsByCardId(userId, cardId);
    }

    // 메인 대시보드 데이터 조회 (Budget 연계) - 컨트롤러용
    public DashboardDTO getDashboardData(Long userId) {
        LocalDate now = LocalDate.now();
        String currentMonth =
                now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastMonth =
                now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));

        // 1. Budget에서 사용자 예산 정보 조회
        BudgetVO budget = budgetService.getBudget(userId);

        // 2. 이번달/저번달 사용금액 ��산
        BigDecimal thisMonthUsage = calculateMonthlyUsage(userId,
                currentMonth);
        BigDecimal lastMonthUsage = calculateMonthlyUsage(userId, lastMonth);

        // 3. 예산이 설정되어 있는 경우 계산
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal remainingBudget = BigDecimal.ZERO;
        BigDecimal usageRate = BigDecimal.ZERO;
        boolean hasBudget = false;
        boolean isOverBudget = false;

        if (budget != null && budget.getTotalBudget() != null) {
            hasBudget = true;
            totalBudget = budget.getTotalBudget()
                    .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)
                    .multiply(BigDecimal.TEN);
            remainingBudget = totalBudget.subtract(thisMonthUsage);
            usageRate = totalBudget.compareTo(BigDecimal.ZERO) > 0 ?
                    thisMonthUsage.divide(totalBudget, 4,
                            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            .setScale(0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            isOverBudget = thisMonthUsage.compareTo(totalBudget) > 0;
        }

        // 4. 일평균 사용금액 계산  1의자리 버림, 소수점 없앰, 10원단위 절삭값으로
        int daysPassed = now.getDayOfMonth();
        BigDecimal dailyAverage = daysPassed > 0
                ? thisMonthUsage
                .divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP) // 1. 일평균 계산
                .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)                    // 2. 10으로 나눈 후 소수점 이하 및 1의 자리 제거
                .multiply(BigDecimal.TEN)                                       // 3. 다시 10을 곱해 10원 단위로 환산
                : BigDecimal.ZERO;

        return DashboardDTO.builder()
                .budgetMonth(budget != null ? budget.getMonth() : null)
                .totalBudget(totalBudget)
                .thisMonthUsage(thisMonthUsage)
                .lastMonthUsage(lastMonthUsage)
                .remainingBudget(remainingBudget)
                .usageRate(usageRate)
                .dailyAverage(dailyAverage)
                .currentMonth(currentMonth)
                .daysInMonth(now.lengthOfMonth())
                .daysPassed(daysPassed)
                .hasBudget(hasBudget)
                .isOverBudget(isOverBudget)
                .build();
    }

    // 특정 월의 총 사용금액 계산
    private BigDecimal calculateMonthlyUsage(Long userId, String month) {
        List<CardApproval> monthlyApprovals =
                cardApprovalMapper.findThisMonthApprovalsByUser(userId, month);

        BigDecimal totalUsage = BigDecimal.ZERO;

        for (CardApproval approval : monthlyApprovals) {
            if (approval.getResUsedAmount() != null) {
                BigDecimal usedAmount = new
                        BigDecimal(approval.getResUsedAmount());

                // 취소된 거래는 차감
                if ("1".equals(approval.getResCancelYN()) &&
                        approval.getResCancelAmount() != null) {
                    BigDecimal canceled = new
                            BigDecimal(approval.getResCancelAmount());
                    usedAmount = usedAmount.subtract(canceled);
                }

                totalUsage = totalUsage.add(usedAmount);
            }
        }

        return totalUsage
                .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)
                .multiply(BigDecimal.TEN);
    }
    
    // ===== 내부 메서드 호출 방식 - 스케줄러에서 사용 =====
    
    /**
     * 내부 메서드 호출 방식 - 예산 모니터링용 데이터 조회
     * 스케줄러에서 예산 초과 여부 판단을 위해 사용
     */
    public BudgetMonitoringDTO getBudgetMonitoringData(Long userId) {
        LocalDate now = LocalDate.now();
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // 1. 예산 정보 조회
        BudgetVO budget = budgetService.getBudget(userId);
        
        // 2. 이번 달 사용금액 계산
        BigDecimal thisMonthUsage = calculateMonthlyUsage(userId, currentMonth);
        
        // 3. 예산 계산
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal usageRate = BigDecimal.ZERO;
        boolean hasBudget = false;
        boolean isOverBudget = false;
        boolean isWarningLevel = false; // 80% 초과 여부
        
        if (budget != null && budget.getTotalBudget() != null) {
            hasBudget = true;
            totalBudget = budget.getTotalBudget()
                    .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)
                    .multiply(BigDecimal.TEN);
            
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                usageRate = thisMonthUsage.divide(totalBudget, 4, RoundingMode.HALF_UP);
                isOverBudget = usageRate.compareTo(BigDecimal.ONE) > 0; // 100% 초과
                isWarningLevel = usageRate.compareTo(BigDecimal.valueOf(0.8)) >= 0; // 80% 이상
            }
        }
        
        return BudgetMonitoringDTO.builder()
                .userId(userId)
                .currentMonth(currentMonth)
                .totalBudget(totalBudget)
                .thisMonthUsage(thisMonthUsage)
                .usageRate(usageRate)
                .hasBudget(hasBudget)
                .isOverBudget(isOverBudget)
                .isWarningLevel(isWarningLevel)
                .build();
    }
    
    /**
     * 내부 메서드 호출 방식 - 새 거래내역 추가 후 예산 체크 트리거
     * 스케줄러에서 fetchAndSaveApprovals 호출 후 사용
     */
    @Transactional
    public boolean fetchAndSaveApprovalsWithBudgetCheck(Long userId, Long cardId) throws Exception {
        List<CardApproval> newApprovals = fetchAndSaveApprovals(userId, cardId);
        return !newApprovals.isEmpty(); // 새로운 거래내역이 있으면 true 반환
    }
    
    /**
     * 내부 메서드 호출 방식 - 사용자의 모든 카드 처리 후 예산 체크 트리거  
     * 스케줄러에서 사용
     */
    @Transactional
    public boolean fetchAndSaveAllCardsWithBudgetCheck(Long userId) {
        List<Long> cardIds = cardApprovalMapper.findCardIdsByUser(userId);
        boolean hasNewTransactions = false;
        
        for (Long cardId : cardIds) {
            try {
                List<CardApproval> newApprovals = fetchAndSaveApprovals(userId, cardId);
                if (!newApprovals.isEmpty()) {
                    hasNewTransactions = true;
                }
            } catch (Exception e) {
                log.error("카드 {} 처리 중 오류", cardId, e);
            }
        }
        
        log.info("사용자 {} 카드 승인내역 동기화 완료 - 새 거래내역 여부: {}", userId, hasNewTransactions);
        return hasNewTransactions;
    }
}
