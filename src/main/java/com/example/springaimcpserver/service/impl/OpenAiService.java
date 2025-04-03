package com.example.springaimcpserver.service.impl;

import com.example.springaimcpserver.exception.DocumentGenerationException;
import com.example.springaimcpserver.service.AiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService implements AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public String generateContent(String prompt) {
        log.debug("Generating content with prompt: {}", prompt);
        Message userMessage = new UserMessage(prompt);
        Prompt aiPrompt = new Prompt(userMessage);
        ChatResponse response = chatClient.call(aiPrompt);
        return response.getResult().getOutput().getContent();
    }

    @Override
    public Map<String, Object> generateStructuredContent(String prompt, String outputFormat) {
        String structuredPrompt = prompt + "\n\n" +
                "반환 형식은 다음과 같아야 합니다: " + outputFormat + "\n" +
                "유효한 JSON 형식으로 반환해 주세요.";

        log.debug("Generating structured content with prompt: {}", structuredPrompt);
        
        String jsonResponse = generateContent(structuredPrompt);
        
        try {
            // JSON 문자열 식별 및 추출
            if (jsonResponse.contains("{") && jsonResponse.contains("}")) {
                int start = jsonResponse.indexOf("{");
                int end = jsonResponse.lastIndexOf("}") + 1;
                jsonResponse = jsonResponse.substring(start, end);
            }
            
            return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", jsonResponse, e);
            throw new DocumentGenerationException("AI 응답을 구조화된 형식으로 변환하는데 실패했습니다.", e);
        }
    }

    @Override
    public Map<String, String> generateDocumentStructure(String title, List<String> sections) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("제목: ").append(title).append("\n\n");
        promptBuilder.append("다음 섹션으로 구성된 문서의 내용을 생성해주세요:\n");
        
        for (String section : sections) {
            promptBuilder.append("- ").append(section).append("\n");
        }
        
        promptBuilder.append("\n각 섹션의 내용을 JSON 형식으로 반환해주세요. 각 섹션은 키가 되며, 값은 해당 섹션의 내용입니다.");
        
        return generateStructuredContent(promptBuilder.toString(), "{ \"section1\": \"content1\", \"section2\": \"content2\", ... }")
                .entrySet().stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().toString()), HashMap::putAll);
    }

    @Override
    public Map<String, List<List<String>>> generateExcelStructure(String title, String content) {
        String prompt = String.format(
                "제목: %s\n\n" +
                "내용: %s\n\n" +
                "위 정보를 기반으로 엑셀 파일의 구조를 생성해주세요.\n" +
                "여러 시트로 구성될 수 있으며, 각 시트에는 행과 열로 구성된 데이터가 포함됩니다.\n" +
                "첫 번째 행은 열 제목이어야 합니다.\n" +
                "JSON 형식으로 반환해주세요. 각 시트는 키가 되며, 값은 2차원 배열로 각 행의 데이터입니다.",
                title, content);

        Map<String, Object> response = generateStructuredContent(prompt, 
                "{ \"Sheet1\": [[\"Column1\", \"Column2\"], [\"Data1\", \"Data2\"]], \"Sheet2\": [[...], [...]] }");
        
        Map<String, List<List<String>>> result = new HashMap<>();
        
        response.forEach((sheetName, sheetData) -> {
            if (sheetData instanceof List) {
                List<List<String>> convertedRows = new ArrayList<>();
                
                for (Object row : (List<?>) sheetData) {
                    if (row instanceof List) {
                        List<String> convertedCells = new ArrayList<>();
                        
                        for (Object cell : (List<?>) row) {
                            convertedCells.add(cell.toString());
                        }
                        
                        convertedRows.add(convertedCells);
                    }
                }
                
                result.put(sheetName, convertedRows);
            }
        });
        
        return result;
    }

    @Override
    public List<Map<String, String>> generatePptStructure(String title, String content) {
        String prompt = String.format(
                "제목: %s\n\n" +
                "내용: %s\n\n" +
                "위 정보를 기반으로 PPT 프레젠테이션의 슬라이드 구조를 생성해주세요.\n" +
                "각 슬라이드에는 제목, 내용, 그리고 선택적으로 메모가 포함될 수 있습니다.\n" +
                "슬라이드 목록을 JSON 형식으로 반환해주세요. 각 슬라이드는 객체여야 하며, 슬라이드 제목, 내용, 메모를 포함합니다.",
                title, content);

        String outputFormat = 
                "[{\"title\": \"슬라이드1 제목\", \"content\": \"슬라이드1 내용\", \"notes\": \"슬라이드1 메모\"}, ...]";
        
        Map<String, Object> response = generateStructuredContent(prompt, outputFormat);
        
        List<Map<String, String>> slides = new ArrayList<>();
        
        if (response.containsKey("slides") && response.get("slides") instanceof List) {
            List<?> rawSlides = (List<?>) response.get("slides");
            
            for (Object slide : rawSlides) {
                if (slide instanceof Map) {
                    Map<?, ?> slideMap = (Map<?, ?>) slide;
                    Map<String, String> convertedSlide = new HashMap<>();
                    
                    slideMap.forEach((k, v) -> convertedSlide.put(k.toString(), v != null ? v.toString() : ""));
                    
                    slides.add(convertedSlide);
                }
            }
        } else {
            // 직접 응답이 슬라이드 배열인 경우를 처리
            for (Map.Entry<String, Object> entry : response.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<?, ?> slideMap = (Map<?, ?>) entry.getValue();
                    Map<String, String> convertedSlide = new HashMap<>();
                    
                    slideMap.forEach((k, v) -> convertedSlide.put(k.toString(), v != null ? v.toString() : ""));
                    
                    slides.add(convertedSlide);
                }
            }
        }
        
        return slides;
    }
}
