package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
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

    private String status;

    @Field("client_ruc")
    private String clientRuc;

    @Field("company_name")
    private String companyName;

    @Field("total_records")
    private Long totalRecords;

    private Long successful;
    private Long rejected;

    @Field("successful_amount")
    private BigDecimal successfulAmount;

    @Field("processed_at")
    private Instant processedAt;

    @Field("completed_at")
    private Instant completedAt;

    public String getId() {
        return id;
    }

    public String getEffectiveBatchId() {
        if (paymentBatchId != null) {
            return paymentBatchId;
        }
        return batchId != null ? batchId : id;
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
        return totalRecords;
    }

    public Long getSuccessful() {
        return successful;
    }

    public Long getRejected() {
        return rejected;
    }

    public BigDecimal getSuccessfulAmount() {
        return successfulAmount;
    }

    public Instant getProcessedAt() {
        return processedAt != null ? processedAt : completedAt;
    }
}
