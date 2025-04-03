package com.example.springaimcpserver.service.impl;

import com.example.springaimcpserver.exception.DocumentGenerationException;
import com.example.springaimcpserver.model.DocumentRequest;
import com.example.springaimcpserver.model.DocumentResponse;
import com.example.springaimcpserver.service.AiService;
import com.example.springaimcpserver.service.DocumentGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerPointGeneratorService implements DocumentGeneratorService {

    private final AiService aiService;
    
    @Value("${app.document.temp-dir}")
    private String tempDir;
    
    // 문서 상태를 저장하는 인메모리 저장소
    private final Map<String, DocumentResponse> documentStatusMap = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<DocumentResponse> generateDocument(DocumentRequest request) {
        if (request.getDocumentType() != DocumentRequest.DocumentType.POWERPOINT) {
            throw new IllegalArgumentException("파워포인트 문서 생성 요청이 아닙니다.");
        }

        String documentId = UUID.randomUUID().toString();
        DocumentResponse processingResponse = DocumentResponse.processing(documentId, request.getTitle());
        documentStatusMap.put(documentId, processingResponse);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // AI 서비스를 통해 PPT 구조 생성
                List<Map<String, String>> pptStructure = aiService.generatePptStructure(
                        request.getTitle(), request.getContent());
                
                // 실제 PPT 파일 생성
                String fileName = createPowerPointFile(request.getTitle(), pptStructure);
                
                // 완료 응답 생성
                String fileUrl = "/api/documents/ppt/" + fileName;
                String downloadUrl = "/api/documents/ppt/download/" + fileName;
                
                DocumentResponse completedResponse = DocumentResponse.completed(
                        documentId, request.getTitle(), fileName, fileUrl, downloadUrl);
                
                documentStatusMap.put(documentId, completedResponse);
                return completedResponse;
                
            } catch (Exception e) {
                log.error("문서 생성 중 오류 발생: {}", e.getMessage(), e);
                DocumentResponse failedResponse = DocumentResponse.failed(
                        documentId, request.getTitle(), e.getMessage());
                documentStatusMap.put(documentId, failedResponse);
                return failedResponse;
            }
        });
    }

    @Override
    public DocumentResponse getDocumentStatus(String documentId) {
        return documentStatusMap.getOrDefault(documentId, 
                DocumentResponse.failed(documentId, "Unknown", "문서를 찾을 수 없습니다."));
    }

    /**
     * PowerPoint 파일을 생성합니다.
     * 
     * @param title 문서 제목
     * @param slides 슬라이드 데이터
     * @return 생성된 파일 이름
     * @throws DocumentGenerationException 문서 생성 중 오류 발생 시
     */
    private String createPowerPointFile(String title, List<Map<String, String>> slides) throws DocumentGenerationException {
        // 파일 이름 생성 (현재 시간 포함)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = title.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        String fileName = safeName + "_" + timestamp + ".pptx";
        
        // 임시 디렉토리 확인
        Path dirPath = Paths.get(tempDir);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new DocumentGenerationException("임시 디렉토리 생성 실패: " + e.getMessage(), e);
            }
        }
        
        // PowerPoint 파일 생성
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            // 마스터 슬라이드 레이아웃 설정
            XSLFSlideMaster defaultMaster = ppt.getSlideMasters().get(0);
            XSLFSlideLayout titleLayout = defaultMaster.getLayout(SlideLayout.TITLE);
            XSLFSlideLayout titleAndContentLayout = defaultMaster.getLayout(SlideLayout.TITLE_AND_CONTENT);
            
            // 첫 번째 슬라이드: 제목 슬라이드
            XSLFSlide titleSlide = ppt.createSlide(titleLayout);
            XSLFTextShape titleShape = titleSlide.getPlaceholder(0);
            titleShape.setText(title);
            titleShape.setFillColor(new Color(240, 240, 240));
            
            // 스타일 설정
            XSLFTextParagraph titleParagraph = titleShape.getTextParagraphs().get(0);
            titleParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
            
            XSLFTextRun titleRun = titleParagraph.getTextRuns().get(0);
            titleRun.setFontSize(44.);
            titleRun.setFontFamily("맑은 고딕");
            titleRun.setBold(true);
            titleRun.setFontColor(new Color(44, 62, 80));
            
            // 슬라이드 생성
            for (Map<String, String> slideData : slides) {
                String slideTitle = slideData.getOrDefault("title", "");
                String slideContent = slideData.getOrDefault("content", "");
                String slideNotes = slideData.getOrDefault("notes", "");
                
                XSLFSlide slide = ppt.createSlide(titleAndContentLayout);
                
                // 제목 설정
                XSLFTextShape titlePlaceholder = slide.getPlaceholder(0);
                if (titlePlaceholder != null) {
                    titlePlaceholder.setText(slideTitle);
                    titlePlaceholder.setFillColor(new Color(240, 240, 240));
                    
                    XSLFTextParagraph slideTitleParagraph = titlePlaceholder.getTextParagraphs().get(0);
                    XSLFTextRun slideTitleRun = slideTitleParagraph.getTextRuns().get(0);
                    slideTitleRun.setFontSize(32.);
                    slideTitleRun.setFontFamily("맑은 고딕");
                    slideTitleRun.setBold(true);
                    slideTitleRun.setFontColor(new Color(44, 62, 80));
                }
                
                // 내용 설정
                XSLFTextShape contentPlaceholder = slide.getPlaceholder(1);
                if (contentPlaceholder != null) {
                    contentPlaceholder.clearText();
                    
                    // 내용 줄 단위로 분할
                    String[] lines = slideContent.split("\n");
                    boolean isFirstLine = true;
                    
                    for (String line : lines) {
                        XSLFTextParagraph paragraph = isFirstLine ? 
                                contentPlaceholder.addNewTextParagraph() : 
                                contentPlaceholder.addNewTextParagraph();
                        
                        XSLFTextRun run = paragraph.addNewTextRun();
                        run.setText(line);
                        run.setFontSize(20.);
                        run.setFontFamily("맑은 고딕");
                        
                        isFirstLine = false;
                    }
                }
                
                // 슬라이드 노트 추가
                if (!slideNotes.isBlank()) {
                    XSLFNotes notes = ppt.getNotesSlide(slide);
                    XSLFTextShape notesShape = notes.getPlaceholder(1);
                    notesShape.setText(slideNotes);
                }
            }
            
            // 파일 저장
            File outputFile = new File(tempDir, fileName);
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                ppt.write(fileOut);
            }
            
            return fileName;
            
        } catch (IOException e) {
            throw new DocumentGenerationException("PowerPoint 파일 생성 실패: " + e.getMessage(), e);
        }
    }
}
