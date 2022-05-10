package com.jeff.recommender.domain.repository;

import com.jeff.recommender.domain.model.Recommendation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface RecommendationRepository {

    Optional<Recommendation> findById(UUID id);

    Optional<Recommendation> findLastByCustomer(UUID customerId);
    Stream<Recommendation> findByCustomer(UUID customerId, int page, int size);

    Recommendation save(Recommendation recommendation);

}
