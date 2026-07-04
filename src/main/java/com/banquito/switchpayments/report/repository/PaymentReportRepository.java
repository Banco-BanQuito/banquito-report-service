package com.banquito.switchpayments.report.repository;

import com.banquito.switchpayments.report.model.PaymentReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentReportRepository extends MongoRepository<PaymentReport, String> {
}
