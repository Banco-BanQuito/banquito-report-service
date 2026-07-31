package com.banquito.switchpayments.report.service;

import com.banquito.payswitch.notification.NotificationResponse;
import com.banquito.switchpayments.report.exception.BatchNotCompletedException;
import com.banquito.switchpayments.report.exception.ReportNotFoundException;
import com.banquito.switchpayments.report.model.PaymentReport;
import com.banquito.switchpayments.report.model.ReceiptResponse;
import com.banquito.switchpayments.report.repository.BeneficiaryNotificationRepository;
import com.banquito.switchpayments.report.repository.PaymentReportRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PaymentReportRepository paymentReportRepository;
    @Mock
    private BeneficiaryNotificationRepository notificationRepository;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private DetailStatusLogReader detailStatusLogReader;
    @Mock
    private MongoTemplate mongoTemplate;

    @TempDir
    Path tempDir;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(paymentReportRepository, notificationRepository, notificationClient,
                detailStatusLogReader, mongoTemplate, tempDir.toString());
    }

    private Document detailDocument(String id, String status, String email, String amount) {
        Document doc = new Document();
        doc.put("_id", id);
        doc.put("payment_batch_id", "BATCH-1");
        doc.put("line_number", 1);
        doc.put("transaction_id", "TX-" + id);
        doc.put("beneficiary_name", "Beneficiario " + id);
        doc.put("beneficiary_email", email);
        doc.put("amount", new BigDecimal(amount));
        doc.put("status", status);
        doc.put("processed_at", Instant.parse("2026-07-18T10:00:00Z"));
        return doc;
    }

    private Document batchDocument(String status) {
        Document doc = new Document();
        doc.put("_id", "BATCH-1");
        doc.put("status", status);
        doc.put("client_ruc", "1791112223001");
        doc.put("company_name", "ACME Corp");
        doc.put("total_records", 1L);
        doc.put("successful", 1L);
        doc.put("rejected", 0L);
        doc.put("successful_amount", new BigDecimal("100.00"));
        return doc;
    }

    @Test
    void generateNewsReportCsvWritesFileAndReturnsCsvBytes() {
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("payment_dispatch_detail")))
                .thenReturn(List.of(detailDocument("D1", "SUCCESS", "a@example.com", "50.00")));
        when(detailStatusLogReader.latestByDetailId("BATCH-1")).thenReturn(Map.of());

        byte[] csv = service.generateNewsReportCsv("BATCH-1");

        assertThat(new String(csv)).contains("TX-D1");
    }

    @Test
    void generateNewsReportCsvThrowsWhenNoDetailsInAnyCollection() {
        when(mongoTemplate.find(any(Query.class), eq(Document.class), anyString())).thenReturn(List.of());

        assertThatThrownBy(() -> service.generateNewsReportCsv("BATCH-EMPTY"))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    void generateReceiptComputesTotalsForCompletedBatch() {
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("payment_dispatch_detail")))
                .thenReturn(List.of(detailDocument("D1", "SUCCESS", "a@example.com", "100.00")));
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("payment_dispatch_batch")))
                .thenReturn(batchDocument("COMPLETED"));
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("file_payment_batch"))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("routing_payment_batch"))).thenReturn(null);
        when(notificationRepository.findFirstByPaymentDetailIdAndStatus(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(notificationClient.sendPaymentNotification(any()))
                .thenReturn(NotificationResponse.newBuilder().setStatus("ENVIADO").build());

        ReceiptResponse receipt = service.generateReceipt("BATCH-1");

        assertThat(receipt.companyName()).isEqualTo("ACME Corp");
        assertThat(receipt.commissionCharged()).isEqualByComparingTo("0.60");
        assertThat(receipt.totalDebited()).isEqualByComparingTo("100.69");
    }

    @Test
    void generateReceiptThrowsWhenBatchNotCompleted() {
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("payment_dispatch_detail")))
                .thenReturn(List.of(detailDocument("D1", "SUCCESS", "a@example.com", "10.00")));
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("payment_dispatch_batch")))
                .thenReturn(batchDocument("PENDING"));

        assertThatThrownBy(() -> service.generateReceipt("BATCH-1"))
                .isInstanceOf(BatchNotCompletedException.class);
    }

    @Test
    void generateReceiptPdfReturnsPdfBytes() {
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("payment_dispatch_detail")))
                .thenReturn(List.of(detailDocument("D1", "SUCCESS", "a@example.com", "10.00")));
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("payment_dispatch_batch")))
                .thenReturn(batchDocument("COMPLETED"));
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("file_payment_batch"))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("routing_payment_batch"))).thenReturn(null);
        when(notificationRepository.findFirstByPaymentDetailIdAndStatus(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(notificationClient.sendPaymentNotification(any()))
                .thenReturn(NotificationResponse.newBuilder().setStatus("ENVIADO").build());

        byte[] pdf = service.generateReceiptPdf("BATCH-1");

        assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }
}
