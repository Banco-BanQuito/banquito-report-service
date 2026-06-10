package com.banquito.report.config;

import com.banquito.payswitch.notification.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationGrpcConfig {

    private ManagedChannel channel;

    @Bean
    public NotificationServiceGrpc.NotificationServiceBlockingStub notificationBlockingStub(
            @Value("${banquito.notification.grpc-host}") String host,
            @Value("${banquito.notification.grpc-port}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        return NotificationServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
