package com.savit.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI Chat Completions API의 메시지 객체
 * 요청과 응답 모두에서 사용됨!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    
    /**
     * 메시지 역할
     * - "system": 시스템 지시사항
     * - "user": 사용자 메시지
     * - "assistant": AI 어시스턴트 응답
     */
    private String role;
    
    /**
     * 메시지 내용
     */
    private String content;
    
    /**
     * 메시지 이름 (선택적)
     * 함수 호출이나 특정 사용자 식별용
     */
    private String name;
    

    public static Message user(String content) {
        return Message.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    public static Message assistant(String content) {
        return Message.builder()
                .role("assistant")
                .content(content)
                .build();
    }
    
    public static Message system(String content) {
        return Message.builder()
                .role("system")
                .content(content)
                .build();
    }
}