package com.example.springaimcpserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {

    private String id;
    private String title;
    private String fileName;
    private String fileUrl;
    private String downloadUrl;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    public enum DocumentStatus {
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public static DocumentResponse processing(String id, String title) {
        return DocumentResponse.builder()
                .id(id)
                .title(title)
                .status(DocumentStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static DocumentResponse completed(String id, String title, String fileName, String fileUrl, String downloadUrl) {
        return DocumentResponse.builder()
                .id(id)
                .title(title)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .downloadUrl(downloadUrl)
                .status(DocumentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static DocumentResponse failed(String id, String title, String errorMessage) {
        return DocumentResponse.builder()
                .id(id)
                .title(title)
                .status(DocumentStatus.FAILED)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }
}
