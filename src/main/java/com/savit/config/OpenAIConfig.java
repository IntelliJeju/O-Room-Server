package com.savit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Configuration
@Getter
public class OpenAIConfig {
    
    @Value("${llm.enabled:false}")
    private boolean enabled;

    @Value("${llm.openai.api.key}")
    private String apiKey;

    @Value("${llm.openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${llm.openai.max-tokens:200}")
    private int maxTokens;

    @Value("${llm.openai.temperature:0.7}")
    private double temperature;
    
    /**
     * OpenAI API 요청/응답 처리용 ObjectMapper
     * JSON 직렬화 위해 사용
     */
    @Bean("openaiObjectMapper")
    public ObjectMapper openaiObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
