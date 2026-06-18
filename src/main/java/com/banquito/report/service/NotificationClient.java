package com.banquito.report.service;

import com.banquito.payswitch.notification.NotificationRequest;
import com.banquito.payswitch.notification.NotificationResponse;
import com.banquito.payswitch.notification.NotificationServiceGrpc;
import com.banquito.report.model.PaymentDetail;
import io.grpc.StatusRuntimeException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationClient {

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;
    private final boolean enabled;

    public NotificationClient(NotificationServiceGrpc.NotificationServiceBlockingStub stub,
                              @Value("${banquito.notification.grpc-enabled}") boolean enabled) {
        this.stub = stub;
        this.enabled = enabled;
    }

    public NotificationResponse sendPaymentNotification(PaymentDetail detail) {
        if (!enabled || detail.getBeneficiaryEmail() == null || detail.getBeneficiaryEmail().isBlank()) {
            return NotificationResponse.newBuilder()
                    .setNotificationId("")
                    .setStatus("OMITIDO")
                    .build();
        }

        try {
            return stub.sendNotification(NotificationRequest.newBuilder()
                    .setPaymentDetailId(detail.getId() == null ? 0 : detail.getId().hashCode())
                    .setEmailTo(detail.getBeneficiaryEmail())
                    .setSubject("Pago recibido - BanQuito")
                    .setBodyTemplate("BENEFICIARY_PAYMENT")
                    .putAllVariables(Map.of(
                            "amount", detail.getAmount() == null ? "" : detail.getAmount().toPlainString(),
                            "companyName", safe(detail.getCompanyName()),
                            "concept", safe(detail.getConcept()),
                            "date", detail.getProcessedAt() == null ? "" : detail.getProcessedAt().toString()
                    ))
                    .build());
        } catch (StatusRuntimeException ex) {
            return NotificationResponse.newBuilder()
                    .setNotificationId("")
                    .setStatus("ERROR")
                    .build();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
