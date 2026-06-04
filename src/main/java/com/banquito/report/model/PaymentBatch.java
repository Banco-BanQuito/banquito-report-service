package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payment_batch")
public class PaymentBatch {

    @Id
    private String id;

    @Field("batch_id")
    private String batchId;

    @Field("payment_batch_id")
    private String paymentBatchId;

    @Field("batchId")
    private String batchIdCamel;

    private String status;

    @Field("client_ruc")
    private String clientRuc;

    @Field("company_name")
    private String companyName;

    @Field("total_records")
    private Long totalRecords;

    @Field("declaredTotalRecords")
    private Long declaredTotalRecords;

    private Long successful;
    private Long rejected;

    @Field("successfulRecords")
    private Long successfulRecords;

    @Field("rejectedRecords")
    private Long rejectedRecords;

    @Field("successful_amount")
    private BigDecimal successfulAmount;

    @Field("successfulAmount")
    private BigDecimal successfulAmountCamel;

    @Field("processed_at")
    private Instant processedAt;

    @Field("completed_at")
    private Instant completedAt;

    @Field("completedAt")
    private LocalDateTime completedAtCamel;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public String getEffectiveBatchId() {
        if (paymentBatchId != null) {
            return paymentBatchId;
        }
        if (batchId != null) {
            return batchId;
        }
        return batchIdCamel != null ? batchIdCamel : id;
    }

    public String getStatus() {
        return status;
    }

    public String getClientRuc() {
        return clientRuc;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Long getTotalRecords() {
        return totalRecords != null ? totalRecords : declaredTotalRecords;
    }

    public Long getSuccessful() {
        return successful != null ? successful : successfulRecords;
    }

    public Long getRejected() {
        return rejected != null ? rejected : rejectedRecords;
    }

    public BigDecimal getSuccessfulAmount() {
        return successfulAmount != null ? successfulAmount : successfulAmountCamel;
    }

    public Instant getProcessedAt() {
        if (processedAt != null) {
            return processedAt;
        }
        if (completedAt != null) {
            return completedAt;
        }
        if (completedAtCamel != null) {
            return completedAtCamel.toInstant(ZoneOffset.UTC);
        }
        return updatedAt == null ? null : updatedAt.toInstant(ZoneOffset.UTC);
    }
}
