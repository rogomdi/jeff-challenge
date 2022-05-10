package com.jeff.recommender.infrastructure.repository;

import com.jeff.recommender.infrastructure.model.RecommendationDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringRecommendationRepository
    extends MongoRepository<RecommendationDocument, UUID> {

  Slice<RecommendationDocument> findByCustomerId(UUID customerId, Pageable pageable);
}
