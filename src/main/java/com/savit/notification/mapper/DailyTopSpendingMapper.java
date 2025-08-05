package com.savit.notification.mapper;

import com.savit.notification.domain.DailyTopSpending;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DailyTopSpendingMapper {
    
    // 일일 최고 지출 데이터 저장
    void insertDailyTopSpending(DailyTopSpending dailyTopSpending);
    
    // 알림 미발송 데이터 조회 (09시 알림용)
    List<DailyTopSpending> findPendingNotifications();
    
    // 알림 발송 완료 처리
    void markNotificationSent(@Param("id") Long id);
    
    // 중복 체크 (같은 사용자, 같은 날짜)
    boolean existsByUserAndDate(@Param("userId") Long userId, 
                               @Param("targetDate") String targetDate);
}