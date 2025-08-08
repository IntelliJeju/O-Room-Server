package com.savit.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequestDTO {

    /**
     * 사용할 모델 : gpt 4o mini
     */
    private String model;
    
    /**
     * 대화 메시지 리스트
     */
    private List<Message> messages;
    
    /**
     * 최대 생성할 토큰 수
     * 500 으로 고정, 향후 다른 분석이 필요하면 늘리기
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    /**
     * 창의성 수준 (0.0-1.0)
     * 0.7로 고정해서 사용(application.properties)
     */
    private Double temperature;


    /**
     * 단일 사용자 메시지로 요청 생성
     */
    public static OpenAIRequestDTO createSingleMessage(String model, String prompt, Integer maxTokens, Double temperature) {
        Message userMessage = Message.builder()
                .role("user")
                .content(prompt)
                .build();
        
        return OpenAIRequestDTO.builder()
                .model(model)
                .messages(List.of(userMessage))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }
}