package com.banquito.report.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReportNotFound(ReportNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler(BatchNotCompletedException.class)
    public ResponseEntity<Map<String, String>> handleBatchNotCompleted(BatchNotCompletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(MESSAGE_KEY, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
    }
}
