package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "routing_payment_batch")
public class PaymentBatch {

    private static final String LEGACY_DATE_CLASS = "java.util.Date";

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

    public static PaymentBatch fromDocument(org.bson.Document document) {
        PaymentBatch batch = new PaymentBatch();
        Object objectId = document.get("_id");
        batch.id = objectId == null ? null : objectId.toString();
        batch.batchId = document.getString("batch_id");
        batch.paymentBatchId = document.getString("payment_batch_id");
        batch.batchIdCamel = document.getString("batchId");
        batch.status = document.getString("status");
        batch.clientRuc = document.getString("client_ruc");
        batch.companyName = document.getString("company_name");
        batch.totalRecords = longValue(document.get("total_records"));
        batch.declaredTotalRecords = longValue(document.get("declaredTotalRecords"));
        batch.successful = longValue(document.get("successful"));
        batch.rejected = longValue(document.get("rejected"));
        batch.successfulRecords = longValue(document.get("successfulRecords"));
        batch.rejectedRecords = longValue(document.get("rejectedRecords"));
        batch.successfulAmount = bigDecimalValue(document.get("successful_amount"));
        batch.successfulAmountCamel = bigDecimalValue(document.get("successfulAmount"));
        batch.processedAt = instantValue(document.get("processed_at"));
        batch.completedAt = instantValue(document.get("completed_at"));
        batch.completedAtCamel = localDateTimeValue(document.get("completedAt"));
        batch.updatedAt = localDateTimeValue(document.get("updatedAt"));
        return batch;
    }

    private static Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static BigDecimal bigDecimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return value instanceof Number number ? BigDecimal.valueOf(number.doubleValue()) : null;
    }

    private static Instant instantValue(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        Instant legacyDate = legacyDateInstant(value);
        if (legacyDate != null) {
            return legacyDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return null;
    }

    private static LocalDateTime localDateTimeValue(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        Instant legacyDate = legacyDateInstant(value);
        if (legacyDate != null) {
            return LocalDateTime.ofInstant(legacyDate, ZoneOffset.UTC);
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    private static Instant legacyDateInstant(Object value) {
        if (value == null || !LEGACY_DATE_CLASS.equals(value.getClass().getName())) {
            return null;
        }
        try {
            Object epochMillis = value.getClass().getMethod("getTime").invoke(value);
            return epochMillis instanceof Long millis ? Instant.ofEpochMilli(millis) : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
