package com.banquito.report.service;

import com.banquito.report.model.DetailStatusSnapshot;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class DetailStatusLogReader {

    private final MongoTemplate mongoTemplate;

    public DetailStatusLogReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, DetailStatusSnapshot> latestByDetailId(String batchId) {
        Map<String, DetailStatusSnapshot> snapshots = new HashMap<>();
        readCollection("detail_status_log", batchId, snapshots);
        readCollection("batch_status_log", batchId, snapshots);
        return snapshots;
    }

    private void readCollection(String collection, String batchId, Map<String, DetailStatusSnapshot> snapshots) {
        if (!mongoTemplate.collectionExists(collection)) {
            return;
        }

        Query query = new Query(new Criteria().orOperator(
                Criteria.where("payment_batch_id").is(batchId),
                Criteria.where("batch_id").is(batchId)
        ));
        List<Document> logs = mongoTemplate.find(query, Document.class, collection);
        logs.stream()
                .map(this::toSnapshot)
                .filter(snapshot -> snapshot.paymentDetailId() != null && !snapshot.paymentDetailId().isBlank())
                .sorted(Comparator.comparing(snapshot -> snapshot.processedAt() == null ? Instant.EPOCH : snapshot.processedAt()))
                .forEach(snapshot -> snapshots.put(snapshot.paymentDetailId(), snapshot));
    }

    private DetailStatusSnapshot toSnapshot(Document document) {
        return new DetailStatusSnapshot(
                firstString(document, "payment_detail_id", "paymentDetailId", "detail_id", "detailId"),
                firstString(document, "status", "new_status", "current_status"),
                firstString(document, "error_code", "errorCode", "code"),
                firstString(document, "error_description", "errorDescription", "message", "reason"),
                firstInstant(document, "processed_at", "created_at", "changed_at", "timestamp")
        );
    }

    private String firstString(Document document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Instant firstInstant(Document document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value instanceof Instant instant) {
                return instant;
            }
            if (value instanceof java.util.Date date) {
                return date.toInstant();
            }
            if (value != null) {
                try {
                    return Instant.parse(value.toString());
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
