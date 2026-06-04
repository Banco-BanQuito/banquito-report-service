package com.banquito.report.service;

import com.banquito.report.grpc.NotificationServiceGrpc;
import com.banquito.report.grpc.SendNotificationRequest;
import com.banquito.report.grpc.SendNotificationResponse;
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

    public SendNotificationResponse sendPaymentNotification(PaymentDetail detail) {
        if (!enabled || detail.getBeneficiaryEmail() == null || detail.getBeneficiaryEmail().isBlank()) {
            return SendNotificationResponse.newBuilder()
                    .setNotificationId("")
                    .setStatus("OMITIDO")
                    .setErrorMessage("gRPC deshabilitado o email vacio")
                    .build();
        }

        try {
            return stub.sendNotification(SendNotificationRequest.newBuilder()
                    .setPaymentDetailId(detail.getId())
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
            return SendNotificationResponse.newBuilder()
                    .setNotificationId("")
                    .setStatus("ERROR")
                    .setErrorMessage(ex.getStatus().getDescription() == null ? ex.getMessage() : ex.getStatus().getDescription())
                    .build();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
