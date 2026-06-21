package com.banquito.report;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "banquito.notification.grpc-enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/routingdb"
})
class ReportServiceApplicationTests {

    @Test
    void contextLoads() {
        // Spring Boot levanta el contexto con gRPC desactivado para validar la configuracion base.
    }
}
