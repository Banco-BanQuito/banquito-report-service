package com.banquito.report.repository;

import com.banquito.report.model.PaymentBatch;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PaymentBatchRepository extends MongoRepository<PaymentBatch, String> {

    @Query("{ '$or': [ { '_id': ?0 }, { 'batch_id': ?0 }, { 'payment_batch_id': ?0 } ] }")
    Optional<PaymentBatch> findByAnyBatchId(String batchId);
}
