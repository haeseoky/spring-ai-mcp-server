package com.example.springaimcpserver.service;

import com.example.springaimcpserver.model.DocumentRequest;
import com.example.springaimcpserver.service.impl.ExcelGeneratorService;
import com.example.springaimcpserver.service.impl.PowerPointGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 문서 유형에 따라 적절한 문서 생성 서비스를 제공하는 팩토리 클래스
 */
@Component
@RequiredArgsConstructor
public class DocumentGeneratorFactory {

    private final ExcelGeneratorService excelGeneratorService;
    private final PowerPointGeneratorService powerPointGeneratorService;

    /**
     * 문서 유형에 따라 적절한 문서 생성 서비스를 반환합니다.
     *
     * @param documentType 문서 유형
     * @return 문서 생성 서비스
     * @throws IllegalArgumentException 지원하지 않는 문서 유형인 경우
     */
    public DocumentGeneratorService getGenerator(DocumentRequest.DocumentType documentType) {
        switch (documentType) {
            case EXCEL:
                return excelGeneratorService;
            case POWERPOINT:
                return powerPointGeneratorService;
            default:
                throw new IllegalArgumentException("지원하지 않는 문서 유형: " + documentType);
        }
    }
}
