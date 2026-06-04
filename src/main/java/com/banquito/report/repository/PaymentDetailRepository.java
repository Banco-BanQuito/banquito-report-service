package com.banquito.report.repository;

import com.banquito.report.model.PaymentDetail;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PaymentDetailRepository extends MongoRepository<PaymentDetail, String> {

    @Query("{ '$or': [ { 'payment_batch_id': ?0 }, { 'batch_id': ?0 } ] }")
    List<PaymentDetail> findByAnyBatchId(String batchId);
}
