package com.savit.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardApprovalRequestDTO { // Codef 승인내역 API 요청용 DTO
    // Codef 승인내역 API 필수 필드들
    private String organization;    // 카드사 코드 (신한 : "0306")
    private String connectedId;     // 커넥티드 아이디
    private String birthDate;       // 생년월일 YYYYMMDD
    private String startDate;       // 조회시작일 YYYYMMDD
    private String endDate;         // 조회종료일 YYYYMMDD
    private String orderBy;         // 정렬 : "0" 최신순
    private String inquiryType;     // 조회구분 "0" 고정 카드별 조회
    private String cardNo;          // 카드번호 RSA 암호화필수
    private String cardName;        // 보유카드에서 가져온 카드명
    private String cardPassword;    // 카드비번 앞2자리 RSA 암호화 필수
    private String memberStoreInfoType;  // 가맹점정보포함여부 "1" 고정 (포함)
}