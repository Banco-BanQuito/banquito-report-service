package com.banquito.report.model;

import java.time.Instant;

public record DetailStatusSnapshot(
        String paymentDetailId,
        String status,
        String errorCode,
        String errorDescription,
        Instant processedAt
) {
}
