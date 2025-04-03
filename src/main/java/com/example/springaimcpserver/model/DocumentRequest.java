package com.example.springaimcpserver.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;
    
    @NotNull(message = "문서 유형은 필수 입력값입니다.")
    private DocumentType documentType;
    
    private String templateName;
    
    @Builder.Default
    private List<String> sections = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Object> additionalOptions = null;
    
    public enum DocumentType {
        EXCEL, 
        POWERPOINT
    }
}
