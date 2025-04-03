package com.example.springaimcpserver.service;

import java.util.List;
import java.util.Map;

/**
 * AI 서비스 인터페이스
 */
public interface AiService {

    /**
     * 주어진 프롬프트에 대한 AI 응답을 가져옵니다.
     *
     * @param prompt    AI에게 전달할 프롬프트
     * @return          AI의 응답 텍스트
     */
    String generateContent(String prompt);

    /**
     * 주어진 프롬프트와 구조화된 출력 형식에 따라 AI 응답을 가져옵니다.
     *
     * @param prompt        AI에게 전달할 프롬프트
     * @param outputFormat  응답의 구조화된 형식 (JSON 등)
     * @return              구조화된 AI 응답
     */
    Map<String, Object> generateStructuredContent(String prompt, String outputFormat);

    /**
     * 주어진 제목과 섹션으로 문서 구조를 생성합니다.
     *
     * @param title     문서 제목
     * @param sections  문서 섹션 목록
     * @return          섹션별 내용이 담긴 맵
     */
    Map<String, String> generateDocumentStructure(String title, List<String> sections);

    /**
     * 제목과 내용을 바탕으로 엑셀 데이터 구조를 생성합니다.
     *
     * @param title     엑셀 문서 제목
     * @param content   엑셀 내용 설명
     * @return          시트별 데이터가 담긴 맵
     */
    Map<String, List<List<String>>> generateExcelStructure(String title, String content);

    /**
     * 제목과 내용을 바탕으로 PPT 슬라이드 구조를 생성합니다.
     *
     * @param title     PPT 제목
     * @param content   PPT 내용 설명
     * @return          슬라이드별 내용이 담긴 맵
     */
    List<Map<String, String>> generatePptStructure(String title, String content);
}
