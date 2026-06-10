package com.banquito.report.repository;

import com.banquito.report.model.PaymentReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentReportRepository extends MongoRepository<PaymentReport, String> {
}
