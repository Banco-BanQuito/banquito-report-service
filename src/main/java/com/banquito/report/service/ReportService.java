package com.banquito.report.service;

import com.banquito.payswitch.notification.NotificationResponse;
import com.banquito.report.model.BeneficiaryNotification;
import com.banquito.report.model.DetailStatusSnapshot;
import com.banquito.report.model.PaymentBatch;
import com.banquito.report.model.PaymentDetail;
import com.banquito.report.model.PaymentReport;
import com.banquito.report.model.ReceiptResponse;
import com.banquito.report.repository.BeneficiaryNotificationRepository;
import com.banquito.report.repository.PaymentReportRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final BigDecimal COMMISSION_PER_SUCCESS = new BigDecimal("0.60");
    private static final BigDecimal IVA_RATE = new BigDecimal("0.15");
    private static final String STATUS_SENT = "ENVIADO";
    private static final String STATUS_SIMULATED = "SIMULADO";

    private final PaymentReportRepository paymentReportRepository;
    private final BeneficiaryNotificationRepository notificationRepository;
    private final NotificationClient notificationClient;
    private final DetailStatusLogReader detailStatusLogReader;
    private final MongoTemplate mongoTemplate;
    private final Path storagePath;

    public ReportService(PaymentReportRepository paymentReportRepository,
                         BeneficiaryNotificationRepository notificationRepository,
                         NotificationClient notificationClient,
                         DetailStatusLogReader detailStatusLogReader,
                         MongoTemplate mongoTemplate,
                         @Value("${banquito.reports.storage-path}") String storagePath) {
        this.paymentReportRepository = paymentReportRepository;
        this.notificationRepository = notificationRepository;
        this.notificationClient = notificationClient;
        this.detailStatusLogReader = detailStatusLogReader;
        this.mongoTemplate = mongoTemplate;
        this.storagePath = Path.of(storagePath);
    }

    public byte[] generateNewsReportCsv(String batchId) {
        List<PaymentDetail> details = detailsForBatch(batchId);
        Map<String, DetailStatusSnapshot> statusLogs = detailStatusLogReader.latestByDetailId(batchId);
        String reportUuid = UUID.randomUUID().toString();
        Path file = storagePath.resolve("novedades_%s_%s.csv".formatted(batchId, reportUuid));

        StringBuilder csv = new StringBuilder();
        csv.append("LINE_NUMBER,TRANSACTION_ID,BENEFICIARY_NAME,BENEFICIARY_ID,DESTINATION_ACCOUNT,AMOUNT,STATUS,ERROR_CODE,ERROR_DESCRIPTION,PROCESSED_AT\n");
        details.stream()
                .sorted(Comparator.comparing(detail -> detail.getLineNumber() == null ? Integer.MAX_VALUE : detail.getLineNumber()))
                .forEach(detail -> {
                    DetailStatusSnapshot snapshot = statusLogs.get(detail.getId());
                    csv.append(csvValue(detail.getLineNumber()))
                        .append(',').append(csvValue(detail.getTransactionId()))
                        .append(',').append(csvValue(detail.getBeneficiaryName()))
                        .append(',').append(csvValue(detail.getBeneficiaryIdentification()))
                        .append(',').append(csvValue(detail.getDestinationAccount()))
                        .append(',').append(csvValue(detail.getAmount()))
                        .append(',').append(csvValue(firstNonBlank(snapshot == null ? null : snapshot.status(), detail.getStatus())))
                        .append(',').append(csvValue(firstNonBlank(snapshot == null ? null : snapshot.errorCode(), detail.getErrorCode())))
                        .append(',').append(csvValue(firstNonBlank(snapshot == null ? null : snapshot.errorDescription(), detail.getErrorDescription())))
                        .append(',').append(csvValue(snapshot != null && snapshot.processedAt() != null ? snapshot.processedAt() : detail.getProcessedAt()))
                        .append('\n');
                });

        persistReport(batchId, "NOVEDADES", reportUuid, file, csv.toString());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public ReceiptResponse generateReceipt(String batchId) {
        List<PaymentDetail> details = detailsForBatch(batchId);
        Optional<PaymentBatch> batch = batchForId(batchId);
        batch.ifPresent(this::assertCompleted);
        long successful = details.stream().filter(this::isSuccessful).count();
        long rejected = details.size() - successful;
        BigDecimal dispatched = details.stream()
                .filter(this::isSuccessful)
                .map(PaymentDetail::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commission = COMMISSION_PER_SUCCESS.multiply(BigDecimal.valueOf(successful)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = commission.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebited = dispatched.add(commission).add(iva).setScale(2, RoundingMode.HALF_UP);
        Instant generatedAt = Instant.now();
        String receiptUuid = UUID.randomUUID().toString();

        PaymentDetail first = details.getFirst();
        PaymentBatch batchData = batch.orElse(null);
        ReceiptResponse receipt = new ReceiptResponse(
                batchId,
                batchData == null ? "" : nullToEmpty(batchData.getClientRuc()),
                firstNonBlank(batchData == null ? null : batchData.getCompanyName(), first.getCompanyName()),
                processedDate(batchData, generatedAt),
                batchData != null && batchData.getTotalRecords() != null ? batchData.getTotalRecords() : details.size(),
                batchData != null && batchData.getSuccessful() != null ? batchData.getSuccessful() : successful,
                batchData != null && batchData.getRejected() != null ? batchData.getRejected() : rejected,
                batchData != null && batchData.getSuccessfulAmount() != null ? batchData.getSuccessfulAmount().setScale(2, RoundingMode.HALF_UP) : dispatched.setScale(2, RoundingMode.HALF_UP),
                commission,
                iva,
                totalDebited,
                receiptUuid,
                generatedAt
        );

        paymentReportRepository.save(new PaymentReport(batchId, "COMPROBANTE", receiptUuid, "", "GENERADO", generatedAt));
        notifySuccessfulBeneficiaries(details);
        return receipt;
    }

    private void assertCompleted(PaymentBatch batch) {
        if (batch.getStatus() == null || !"COMPLETED".equalsIgnoreCase(batch.getStatus())) {
            throw new BatchNotCompletedException("El lote %s aun no esta COMPLETED. Estado actual: %s"
                    .formatted(batch.getEffectiveBatchId(), batch.getStatus()));
        }
    }

    private List<PaymentDetail> detailsForBatch(String batchId) {
        List<PaymentDetail> details = mongoTemplate.find(batchQuery(batchId), org.bson.Document.class, "payment_detail")
                .stream()
                .map(PaymentDetail::fromDocument)
                .toList();
        if (details.isEmpty()) {
            throw new ReportNotFoundException("No existen detalles procesados para el lote " + batchId);
        }
        return details;
    }

    private Optional<PaymentBatch> batchForId(String batchId) {
        org.bson.Document document = mongoTemplate.findOne(batchQuery(batchId), org.bson.Document.class, "payment_batch");
        return Optional.ofNullable(document).map(PaymentBatch::fromDocument);
    }

    private Query batchQuery(String batchId) {
        return new Query(new Criteria().orOperator(
                Criteria.where("_id").is(batchId),
                Criteria.where("payment_batch_id").is(batchId),
                Criteria.where("batch_id").is(batchId),
                Criteria.where("batchId").is(batchId)
        ));
    }

    private void persistReport(String batchId, String type, String reportUuid, Path file, String content) {
        try {
            Files.createDirectories(storagePath);
            Files.writeString(file, content, StandardCharsets.UTF_8);
            paymentReportRepository.save(new PaymentReport(batchId, type, reportUuid, file.toString(), "GENERADO", Instant.now()));
        } catch (IOException ex) {
            paymentReportRepository.save(new PaymentReport(batchId, type, reportUuid, file.toString(), "ERROR", Instant.now()));
            throw new IllegalStateException("No se pudo almacenar el reporte " + file, ex);
        }
    }

    private void notifySuccessfulBeneficiaries(List<PaymentDetail> details) {
        details.stream()
                .filter(this::isSuccessful)
                .forEach(detail -> {
                    boolean alreadySent = notificationRepository
                            .findFirstByPaymentDetailIdAndStatus(detail.getId(), STATUS_SENT)
                            .isPresent()
                            || notificationRepository
                            .findFirstByPaymentDetailIdAndStatus(routingNotificationId(detail), STATUS_SENT)
                            .isPresent();
                    if (alreadySent) {
                        return;
                    }

                    NotificationResponse response = notificationClient.sendPaymentNotification(detail);
                    Instant now = Instant.now();
                    boolean sent = STATUS_SENT.equals(response.getStatus()) || STATUS_SIMULATED.equals(response.getStatus());
                    notificationRepository.save(new BeneficiaryNotification(
                            detail.getId(),
                            detail.getBeneficiaryEmail(),
                            "Pago recibido - BanQuito",
                            "BENEFICIARY_PAYMENT",
                            sent ? STATUS_SENT : response.getStatus(),
                            sent ? 0 : 1,
                            sent ? null : now.plusSeconds(300),
                            now,
                            sent ? now : null,
                            null
                    ));
                });
    }

    private String routingNotificationId(PaymentDetail detail) {
        return detail.getId() == null ? "" : String.valueOf(detail.getId().hashCode());
    }

    private boolean isSuccessful(PaymentDetail detail) {
        return detail.getStatus() != null
                && List.of("SUCCESS", "SUCCESSFUL", "COMPLETED", "PROCESSED", "EXITOSO", "EXITOSA", "APROBADO", "APROBADA")
                .contains(detail.getStatus().toUpperCase());
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : nullToEmpty(fallback);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private LocalDate processedDate(PaymentBatch batch, Instant generatedAt) {
        Instant instant = batch != null && batch.getProcessedAt() != null ? batch.getProcessedAt() : generatedAt;
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static class ReportNotFoundException extends RuntimeException {
        public ReportNotFoundException(String message) {
            super(message);
        }
    }

    public static class BatchNotCompletedException extends RuntimeException {
        public BatchNotCompletedException(String message) {
            super(message);
        }
    }
}
