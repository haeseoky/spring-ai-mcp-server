package com.example.springaimcpserver.exception;

public class DocumentGenerationException extends RuntimeException {
    
    public DocumentGenerationException(String message) {
        super(message);
    }
    
    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
