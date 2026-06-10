package com.banquito.report.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payment_report")
public class PaymentReport {

    @Id
    private String id;

    @Field("payment_batch_id")
    private String paymentBatchId;

    @Field("report_type")
    private String reportType;

    @Field("report_uuid")
    private String reportUuid;

    @Field("storage_reference")
    private String storageReference;

    private String status;

    @Field("generated_at")
    private Instant generatedAt;

    public PaymentReport(String paymentBatchId, String reportType, String reportUuid,
                         String storageReference, String status, Instant generatedAt) {
        this.paymentBatchId = paymentBatchId;
        this.reportType = reportType;
        this.reportUuid = reportUuid;
        this.storageReference = storageReference;
        this.status = status;
        this.generatedAt = generatedAt;
    }

    public String getPaymentBatchId() {
        return paymentBatchId;
    }

    public String getReportType() {
        return reportType;
    }

    public String getReportUuid() {
        return reportUuid;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public String getStatus() {
        return status;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
