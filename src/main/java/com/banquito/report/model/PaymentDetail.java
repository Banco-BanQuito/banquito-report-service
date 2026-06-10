package com.banquito.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payment_detail")
@TypeAlias("ec.edu.espe.banquito.routingservice.model.PaymentDetail")
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

    @Field("destination_account_number")
    private String destinationAccountNumber;

    @Field("destinationAccountNumber")
    private String destinationAccountNumberCamel;

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
        if (destinationAccount != null) {
            return destinationAccount;
        }
        if (accountDestination != null) {
            return accountDestination;
        }
        return destinationAccountNumber != null ? destinationAccountNumber : destinationAccountNumberCamel;
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

    public static PaymentDetail fromDocument(org.bson.Document document) {
        PaymentDetail detail = new PaymentDetail();
        Object objectId = document.get("_id");
        detail.id = objectId == null ? null : objectId.toString();
        detail.paymentBatchId = document.getString("payment_batch_id");
        detail.batchId = document.getString("batch_id");
        detail.batchIdCamel = document.getString("batchId");
        detail.lineNumber = integerValue(document.get("line_number"));
        detail.lineNumberCamel = integerValue(document.get("lineNumber"));
        detail.transactionId = document.getString("transaction_id");
        detail.transactionUuid = document.getString("transactionUuid");
        detail.beneficiaryName = document.getString("beneficiary_name");
        detail.beneficiaryNameCamel = document.getString("beneficiaryName");
        detail.beneficiaryIdentification = document.getString("beneficiary_identification");
        detail.beneficiaryId = document.getString("beneficiary_id");
        detail.beneficiaryEmail = document.getString("beneficiary_email");
        detail.beneficiaryEmailCamel = document.getString("beneficiaryEmail");
        detail.destinationAccount = document.getString("destination_account");
        detail.accountDestination = document.getString("accountDestination");
        detail.destinationAccountNumber = document.getString("destination_account_number");
        detail.destinationAccountNumberCamel = document.getString("destinationAccountNumber");
        detail.amount = bigDecimalValue(document.get("amount"));
        detail.status = document.getString("status");
        detail.errorCode = document.getString("error_code");
        detail.errorDescription = document.getString("error_description");
        detail.errorMessage = document.getString("errorMessage");
        detail.processedAt = instantValue(document.get("processed_at"));
        detail.processedAtCamel = localDateTimeValue(document.get("processedAt"));
        detail.companyName = document.getString("company_name");
        detail.concept = document.getString("concept");
        return detail;
    }

    private static Integer integerValue(Object value) {
        return value instanceof Number number ? number.intValue() : null;
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
        if (value instanceof Date date) {
            return date.toInstant();
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
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }
}
