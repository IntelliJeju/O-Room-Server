package com.savit.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IamportPaymentResponseDTO {
    private String impUid;
    private String merchantUid;
    private long amount;
    private String status;
    private long paidAt;
    private Long challengeId;
    private Long userId;
}