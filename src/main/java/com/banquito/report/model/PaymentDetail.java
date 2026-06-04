package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payment_detail")
public class PaymentDetail {

    @Id
    private String id;

    @Field("payment_batch_id")
    private String paymentBatchId;

    @Field("batch_id")
    private String batchId;

    @Field("line_number")
    private Integer lineNumber;

    @Field("transaction_id")
    private String transactionId;

    @Field("beneficiary_name")
    private String beneficiaryName;

    @Field("beneficiary_identification")
    private String beneficiaryIdentification;

    @Field("beneficiary_id")
    private String beneficiaryId;

    @Field("beneficiary_email")
    private String beneficiaryEmail;

    @Field("destination_account")
    private String destinationAccount;

    private BigDecimal amount;
    private String status;

    @Field("error_code")
    private String errorCode;

    @Field("error_description")
    private String errorDescription;

    @Field("processed_at")
    private Instant processedAt;

    @Field("company_name")
    private String companyName;

    private String concept;

    public String getId() {
        return id;
    }

    public String getEffectiveBatchId() {
        return paymentBatchId != null ? paymentBatchId : batchId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification != null ? beneficiaryIdentification : beneficiaryId;
    }

    public String getBeneficiaryEmail() {
        return beneficiaryEmail;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getConcept() {
        return concept;
    }
}
