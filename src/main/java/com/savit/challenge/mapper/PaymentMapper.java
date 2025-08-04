package com.savit.challenge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface PaymentMapper {
    void insertPayment(
            @Param("merchantUid") String merchantUid,
            @Param("impUid") String impUid,
            @Param("paidAt") Date paidAt,
            @Param("amount") long amount,
            @Param("status") String status,
            @Param("challengeId") long challengeId,
            @Param("userId") long userId
    );
}