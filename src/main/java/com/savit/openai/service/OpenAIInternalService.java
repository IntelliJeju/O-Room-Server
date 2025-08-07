package com.savit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.savit.config.OpenAIConfig;
import com.savit.openai.dto.OpenAIRequestDTO;
import com.savit.openai.dto.OpenAIResponseDTO;
import lombok.RequiredArgsConstructor;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIInternalService {
    
    private final OpenAIConfig openAIConfig;
    
    @Resource(name = "openaiObjectMapper")
    private ObjectMapper objectMapper;
    
    private OkHttpClient httpClient;
    private final List<String> dailyAnswers = new ArrayList<>();

    // 추가 프롬프트 존재 가능성으로 List 로 생성
    private final List<String> predefinedPrompts = Arrays.asList(
            """
                Generate 5 witty and slightly nagging alert messages targeted at young professionals in their 20s and 30s who are in their first 0–3 years of work.
                These messages should be sent when they are using their credit card too frequently, to discourage overspending and encourage budgeting.
                The tone should be playful, casual, and a bit sarcastic — like a friend who’s concerned.
                Include some emojis, Korean-style reactions, and brief messages that feel familiar to people in their 20s. Each message should be one sentence.
                Output the messages without a numbered list. Answer me in Korean
            """
    );
    
    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        log.info("OpenAI Internal Service 초기화 완료");
    }
    
    /**
     * 미리 정의된 프롬프트들에 대한 답변을 생성하여 리스트에 저장
     */
    public void generateAndStoreDailyAnswers() {
        if (!openAIConfig.isEnabled()) {
            log.warn("OpenAI 기능이 비활성화되어 있습니다.");
            return;
        }
        
        log.info("일일 OpenAI 답변 생성을 시작합니다. 총 {}개 프롬프트", predefinedPrompts.size());
        
        // 기존 답변 리스트 초기화
        dailyAnswers.clear();
        
        for (int i = 0; i < predefinedPrompts.size(); i++) {
            String prompt = predefinedPrompts.get(i);
            try {
                log.info("프롬프트 {}/{} 처리 중: {}", i + 1, predefinedPrompts.size(), 
                        prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt);
                
                String answer = callOpenAIAPI(prompt);
                if (answer != null && !answer.trim().isEmpty()) {
                    dailyAnswers.add(answer);
                    log.info("프롬프트 {}/{} 처리 완료", i + 1, predefinedPrompts.size());
                } else {
                    log.warn("프롬프트 {}/{} 처리 실패: 빈 응답", i + 1, predefinedPrompts.size());
                    dailyAnswers.add("죄송합니다. 현재 답변을 생성할 수 없습니다.");
                }
                
                // API 호출 간격 조절 (Rate Limit 방지)
                Thread.sleep(3000);
                
            } catch (Exception e) {
                log.error("프롬프트 처리 중 오류 발생: {}", prompt, e);
                dailyAnswers.add("죄송합니다. 답변 생성 중 오류가 발생했습니다.");
            }
        }
        
        log.info("일일 OpenAI 답변 생성 완료. 총 {}개 답변 저장", dailyAnswers.size());
    }
    
    /**
     * OpenAI API 호출
     */
    private String callOpenAIAPI(String prompt) throws IOException {
        OpenAIRequestDTO requestDTO = OpenAIRequestDTO.createSingleMessage(
                openAIConfig.getModel(),
                prompt,
                openAIConfig.getMaxTokens(),
                openAIConfig.getTemperature()
        );
        
        String jsonBody = objectMapper.writeValueAsString(requestDTO);
        
        RequestBody body = RequestBody.create(
                jsonBody, 
                MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(openAIConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + openAIConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("OpenAI API 호출 실패: HTTP {} - {}", response.code(), errorBody);
                return null;
            }
            
            String responseBody = response.body().string();
            log.debug("OpenAI API 응답: {}", responseBody);
            
            OpenAIResponseDTO responseDTO = objectMapper.readValue(responseBody, OpenAIResponseDTO.class);
            
            if (responseDTO.getChoices() == null || responseDTO.getChoices().isEmpty()) {
                log.error("OpenAI API 응답에 choices가 없습니다.");
                return null;
            }
            
            return responseDTO.getChoices().get(0).getMessage().getContent();
        }
    }
    
    /**
     * 저장된 일일 답변 리스트 반환
     */
    public List<String> getDailyAnswers() {
        return new ArrayList<>(dailyAnswers);
    }
    
    /**
     * 미리 정의된 프롬프트 리스트 반환
     */
    public List<String> getPredefinedPrompts() {
        return new ArrayList<>(predefinedPrompts);
    }
    
    /**
     * 현재 답변 개수 반환
     */
    public int getAnswerCount() {
        return dailyAnswers.size();
    }
    
    /**
     * OpenAI 서비스 활성화 여부 확인
     */
    public boolean isServiceEnabled() {
        return openAIConfig.isEnabled();
    }
    
    /**
     * 단일 프롬프트 테스트용 메서드 (컨트롤러에서 사용)
     */
    public String testSinglePrompt(String prompt) {
        try {
            return callOpenAIAPI(prompt);
        } catch (Exception e) {
            log.error("단일 프롬프트 테스트 중 오류", e);
            return "테스트 중 오류 발생: " + e.getMessage();
        }
    }
}