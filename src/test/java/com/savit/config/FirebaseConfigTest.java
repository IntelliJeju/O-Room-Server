package com.savit.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.savit.notification.mapper.NotificationMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 테스트 전용 Firebase 및 Mapper 설정 클래스.
 * 실제 Firebase Admin SDK 초기화 및 DB 접근을 방지하기 위해,
 * 관련 빈들을 Mock(가짜) 객체로 만들어 Spring Bean으로 등록합니다.
 */
@Configuration
public class FirebaseConfigTest {

    /**
     * FirebaseMessaging의 Mock 객체를 생성하여 Bean으로 등록합니다.
     * @Primary 어노테이션을 사용하여, 여러 설정이 충돌할 경우 이 빈을 우선적으로 사용하도록 합니다.
     * @return Mockito로 생성된 FirebaseMessaging의 Mock 객체
     */
    @Bean
    @Primary
    public FirebaseMessaging firebaseMessaging() {
        return Mockito.mock(FirebaseMessaging.class);
    }

    /**
     * NotificationMapper의 Mock 객체를 생성하여 Bean으로 등록합니다.
     * @Primary 어노테이션을 사용하여, 실제 Mapper 빈 대신 이 Mock 빈을 사용하도록 강제합니다.
     * @return Mockito로 생성된 NotificationMapper의 Mock 객체
     */
    @Bean
    @Primary
    public NotificationMapper notificationMapper() {
        return Mockito.mock(NotificationMapper.class);
    }
}
