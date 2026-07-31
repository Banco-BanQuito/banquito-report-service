package com.banquito.switchpayments.report.service;

import com.banquito.payswitch.notification.NotificationRequest;
import com.banquito.payswitch.notification.NotificationResponse;
import com.banquito.payswitch.notification.NotificationServiceGrpc;
import com.banquito.switchpayments.report.model.PaymentDetail;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

    @Mock
    private NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    private PaymentDetail detailWithEmail(String email) {
        Document doc = new Document();
        doc.put("_id", "D1");
        doc.put("beneficiary_email", email);
        doc.put("amount", new BigDecimal("50.00"));
        return PaymentDetail.fromDocument(doc);
    }

    @Test
    void sendsNotificationAndReturnsStubResponseWhenEnabled() {
        NotificationClient client = new NotificationClient(stub, true);
        NotificationResponse expected = NotificationResponse.newBuilder().setStatus("ENVIADO").build();
        when(stub.sendNotification(any(NotificationRequest.class))).thenReturn(expected);

        assertThat(client.sendPaymentNotification(detailWithEmail("a@example.com"))).isEqualTo(expected);
    }

    @Test
    void returnsOmitidoWhenGrpcDisabled() {
        NotificationClient client = new NotificationClient(stub, false);

        assertThat(client.sendPaymentNotification(detailWithEmail("a@example.com")).getStatus()).isEqualTo("OMITIDO");
    }

    @Test
    void returnsErrorStatusWhenStubThrows() {
        NotificationClient client = new NotificationClient(stub, true);
        when(stub.sendNotification(any(NotificationRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        assertThat(client.sendPaymentNotification(detailWithEmail("a@example.com")).getStatus()).isEqualTo("ERROR");
    }
}
