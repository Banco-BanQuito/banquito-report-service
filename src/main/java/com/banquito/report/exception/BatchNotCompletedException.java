package com.banquito.report.exception;

public class BatchNotCompletedException extends RuntimeException {
    public BatchNotCompletedException(String message) {
        super(message);
    }
}
