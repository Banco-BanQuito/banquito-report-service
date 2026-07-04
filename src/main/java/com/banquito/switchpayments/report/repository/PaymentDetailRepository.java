package com.banquito.switchpayments.report.repository;

import com.banquito.switchpayments.report.model.PaymentDetail;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PaymentDetailRepository extends MongoRepository<PaymentDetail, String> {

    @Query("{ '$or': [ { 'payment_batch_id': ?0 }, { 'batch_id': ?0 }, { 'batchId': ?0 } ] }")
    List<PaymentDetail> findByAnyBatchId(String batchId);
}
