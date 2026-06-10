package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReceiptResponse(
        String batchId,
        String clientRuc,
        String companyName,
        LocalDate processedDate,
        long totalRecords,
        long successful,
        long rejected,
        BigDecimal totalAmountDispatched,
        BigDecimal commissionCharged,
        BigDecimal ivaCharged,
        BigDecimal totalDebited,
        String receiptUuid,
        Instant generatedAt
) {
}
