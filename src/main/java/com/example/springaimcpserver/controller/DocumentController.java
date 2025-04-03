package com.example.springaimcpserver.controller;

import com.example.springaimcpserver.model.DocumentRequest;
import com.example.springaimcpserver.model.DocumentResponse;
import com.example.springaimcpserver.service.DocumentGeneratorFactory;
import com.example.springaimcpserver.service.DocumentGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentGeneratorFactory documentGeneratorFactory;
    
    @Value("${app.document.temp-dir}")
    private String tempDir;

    /**
     * 새 문서 생성 요청을 처리합니다.
     * 
     * @param request 문서 생성 요청 객체
     * @return 문서 생성 응답 (비동기)
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<DocumentResponse>> createDocument(@Valid @RequestBody DocumentRequest request) {
        log.info("문서 생성 요청: {}", request);
        
        DocumentGeneratorService generatorService = documentGeneratorFactory.getGenerator(request.getDocumentType());
        CompletableFuture<DocumentResponse> futureResponse = generatorService.generateDocument(request);
        
        return futureResponse.thenApply(response -> {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.getId())
                    .toUri();
            
            return ResponseEntity.created(location).body(response);
        });
    }

    /**
     * 문서 생성 상태를 조회합니다.
     * 
     * @param documentId 문서 ID
     * @return 문서 상태
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocumentStatus(@PathVariable String documentId) {
        // 모든 생성기 서비스에서 문서 상태 조회 시도
        for (DocumentRequest.DocumentType documentType : DocumentRequest.DocumentType.values()) {
            try {
                DocumentGeneratorService generatorService = documentGeneratorFactory.getGenerator(documentType);
                DocumentResponse response = generatorService.getDocumentStatus(documentId);
                
                if (response.getStatus() != DocumentResponse.DocumentStatus.FAILED || 
                        response.getErrorMessage() == null || 
                        !response.getErrorMessage().contains("찾을 수 없습니다")) {
                    return ResponseEntity.ok(response);
                }
            } catch (Exception e) {
                log.debug("문서 조회 중 오류 발생: {}", e.getMessage());
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    /**
     * 엑셀 문서 파일을 다운로드합니다.
     * 
     * @param fileName 파일 이름
     * @return 파일 리소스
     */
    @GetMapping("/excel/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadExcelFile(@PathVariable String fileName) {
        File file = Paths.get(tempDir, fileName).toFile();
        Resource resource = new FileSystemResource(file);
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * PPT 문서 파일을 다운로드합니다.
     * 
     * @param fileName 파일 이름
     * @return 파일 리소스
     */
    @GetMapping("/ppt/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadPptFile(@PathVariable String fileName) {
        File file = Paths.get(tempDir, fileName).toFile();
        Resource resource = new FileSystemResource(file);
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .body(resource);
    }
}
