package com.savit.notification.domain;

/**
 * 푸시 알림 타입 정의
 */
public enum NotificationType {

    /** 예산 초과 알림 */
    BUDGET_EXCEEDED("budget_exceeded", "예산 초과"),

    /** 카테고리별 예산 경고 (80% 사용 시) */
    CATEGORY_WARNING("category_warning", "카테고리 예산 경고"),

    /** 카드 사용 즉시 알림 */
    CARD_USAGE("card_usage", "카드 사용 알림"),

    /** 랜덤 잔소리 알림 */
    RANDOM_NAGGING("random_nagging", "랜덤 잔소리"),

    /** 일일 리포트 알림 */
    DAILY_REPORT("daily_report", "일일 리포트"),

    /** 월간 리포트 알림 */
    MONTHLY_REPORT("monthly_report", "월간 리포트"),

    /** 목표 달성 알림 */
    GOAL_ACHIEVED("goal_achieved", "목표 달성"),

    /** 시스템 공지 알림 */
    SYSTEM_NOTICE("system_notice", "시스템 공지");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type code: " + code);
    }
}