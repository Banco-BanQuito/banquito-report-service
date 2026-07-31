package com.banquito.switchpayments.report.service;

import com.banquito.switchpayments.report.model.DetailStatusSnapshot;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailStatusLogReaderTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private DetailStatusLogReader reader;

    @BeforeEach
    void setUp() {
        reader = new DetailStatusLogReader(mongoTemplate);
    }

    @Test
    void latestByDetailIdReturnsEmptyMapWhenNoCollectionsExist() {
        when(mongoTemplate.collectionExists(anyString())).thenReturn(false);

        assertThat(reader.latestByDetailId("BATCH-1")).isEmpty();
    }

    @Test
    void latestByDetailIdMapsLogEntriesByPaymentDetailId() {
        when(mongoTemplate.collectionExists("detail_status_log")).thenReturn(true);
        when(mongoTemplate.collectionExists("batch_status_log")).thenReturn(false);

        Document log = new Document();
        log.put("payment_detail_id", "D1");
        log.put("status", "REJECTED");
        log.put("processed_at", Instant.parse("2026-07-18T10:00:00Z"));
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("detail_status_log")))
                .thenReturn(List.of(log));

        Map<String, DetailStatusSnapshot> result = reader.latestByDetailId("BATCH-1");

        assertThat(result.get("D1").status()).isEqualTo("REJECTED");
    }
}
