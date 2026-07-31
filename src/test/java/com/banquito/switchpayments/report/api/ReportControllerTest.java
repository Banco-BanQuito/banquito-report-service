package com.banquito.switchpayments.report.api;

import com.banquito.switchpayments.report.model.ReceiptResponse;
import com.banquito.switchpayments.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private ReportController controller;

    @BeforeEach
    void setUp() {
        controller = new ReportController(reportService);
    }

    @Test
    void newsReportReturnsCsvAttachment() {
        byte[] csv = "LINE_NUMBER\n".getBytes();
        when(reportService.generateNewsReportCsv("BATCH-1")).thenReturn(csv);

        ResponseEntity<byte[]> response = controller.newsReport("BATCH-1");

        assertThat(response.getBody()).isEqualTo(csv);
    }

    @Test
    void receiptDelegatesToReportService() {
        ReceiptResponse expected = new ReceiptResponse("BATCH-1", "1791112223001", "ACME", LocalDate.now(),
                1, 1, 0, new BigDecimal("100.00"), new BigDecimal("0.60"), new BigDecimal("0.09"),
                new BigDecimal("100.69"), "UUID-1", Instant.now());
        when(reportService.generateReceipt("BATCH-1")).thenReturn(expected);

        assertThat(controller.receipt("BATCH-1")).isEqualTo(expected);
    }

    @Test
    void receiptPdfReturnsPdfAttachment() {
        byte[] pdf = "%PDF-1.4".getBytes();
        when(reportService.generateReceiptPdf("BATCH-1")).thenReturn(pdf);

        ResponseEntity<byte[]> response = controller.receiptPdf("BATCH-1");

        assertThat(response.getBody()).isEqualTo(pdf);
    }

    @Test
    void healthReturnsUpStatus() {
        assertThat(controller.health()).containsEntry("status", "UP");
    }
}
