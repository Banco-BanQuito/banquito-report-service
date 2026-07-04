package com.banquito.switchpayments.report.exception;

public class BatchNotCompletedException extends RuntimeException {
    public BatchNotCompletedException(String message) {
        super(message);
    }
}
