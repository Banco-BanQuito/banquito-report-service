package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    @Field("batchId")
    private String batchIdCamel;

    @Field("line_number")
    private Integer lineNumber;

    @Field("lineNumber")
    private Integer lineNumberCamel;

    @Field("transaction_id")
    private String transactionId;

    @Field("transactionUuid")
    private String transactionUuid;

    @Field("beneficiary_name")
    private String beneficiaryName;

    @Field("beneficiaryName")
    private String beneficiaryNameCamel;

    @Field("beneficiary_identification")
    private String beneficiaryIdentification;

    @Field("beneficiary_id")
    private String beneficiaryId;

    @Field("beneficiary_email")
    private String beneficiaryEmail;

    @Field("beneficiaryEmail")
    private String beneficiaryEmailCamel;

    @Field("destination_account")
    private String destinationAccount;

    @Field("accountDestination")
    private String accountDestination;

    private BigDecimal amount;
    private String status;

    @Field("error_code")
    private String errorCode;

    @Field("error_description")
    private String errorDescription;

    @Field("errorMessage")
    private String errorMessage;

    @Field("processed_at")
    private Instant processedAt;

    @Field("processedAt")
    private LocalDateTime processedAtCamel;

    @Field("company_name")
    private String companyName;

    private String concept;

    public String getId() {
        return id;
    }

    public String getEffectiveBatchId() {
        if (paymentBatchId != null) {
            return paymentBatchId;
        }
        return batchId != null ? batchId : batchIdCamel;
    }

    public Integer getLineNumber() {
        return lineNumber != null ? lineNumber : lineNumberCamel;
    }

    public String getTransactionId() {
        return transactionId != null ? transactionId : transactionUuid;
    }

    public String getBeneficiaryName() {
        return beneficiaryName != null ? beneficiaryName : beneficiaryNameCamel;
    }

    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification != null ? beneficiaryIdentification : beneficiaryId;
    }

    public String getBeneficiaryEmail() {
        return beneficiaryEmail != null ? beneficiaryEmail : beneficiaryEmailCamel;
    }

    public String getDestinationAccount() {
        return destinationAccount != null ? destinationAccount : accountDestination;
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
        return errorDescription != null ? errorDescription : errorMessage;
    }

    public Instant getProcessedAt() {
        if (processedAt != null) {
            return processedAt;
        }
        return processedAtCamel == null ? null : processedAtCamel.toInstant(ZoneOffset.UTC);
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getConcept() {
        return concept;
    }
}
