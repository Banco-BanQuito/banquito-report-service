package com.banquito.switchpayments.report.repository;

import com.banquito.switchpayments.report.model.BeneficiaryNotification;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BeneficiaryNotificationRepository extends MongoRepository<BeneficiaryNotification, String> {

    Optional<BeneficiaryNotification> findFirstByPaymentDetailIdAndStatus(String paymentDetailId, String status);
}
