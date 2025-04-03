package com.example.springaimcpserver.service;

import com.example.springaimcpserver.model.DocumentRequest;
import com.example.springaimcpserver.model.DocumentResponse;

import java.util.concurrent.CompletableFuture;

/**
 * 문서 생성 서비스의 인터페이스
 */
public interface DocumentGeneratorService {

    /**
     * 요청에 따라 문서를 생성합니다.
     * 
     * @param request 문서 생성 요청 객체
     * @return 문서 생성 응답 객체 (비동기)
     */
    CompletableFuture<DocumentResponse> generateDocument(DocumentRequest request);
    
    /**
     * 생성된 문서의 상태를 조회합니다.
     * 
     * @param documentId 문서의 ID
     * @return 문서 생성 상태 응답 객체
     */
    DocumentResponse getDocumentStatus(String documentId);
}
