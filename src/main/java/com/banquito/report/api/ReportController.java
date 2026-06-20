package com.banquito.report.api;

import com.banquito.report.model.ReceiptResponse;
import com.banquito.report.service.ReportService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/payments/batches/{batchId}/report")
    public ResponseEntity<byte[]> newsReport(@PathVariable String batchId) {
        byte[] csv = reportService.generateNewsReportCsv(batchId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novedades_%s.csv".formatted(batchId))
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping("/payments/receipts/{batchId}")
    public ReceiptResponse receipt(@PathVariable String batchId) {
        return reportService.generateReceipt(batchId);
    }

    @GetMapping("/payments/receipts/{batchId}/pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable String batchId) {
        byte[] pdf = reportService.generateReceiptPdf(batchId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante_%s.pdf".formatted(batchId))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reports/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "report-service", "version", "2.0");
    }
}
