package com.savit.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Chat Completions API 응답 DTO
 * gpt-4o-mini 모델용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResponseDTO {
    
    /**
     * 응답 ID
     */
    private String id;
    
    /**
     * 객체 타입 (보통 "chat.completion")
     */
    private String object;
    
    /**
     * 생성 시간 (Unix timestamp)
     */
    private Long created;
    
    /**
     * 사용된 모델
     */
    private String model;
    
    /**
     * 응답 선택지 리스트
     */
    private List<Choice> choices;
}