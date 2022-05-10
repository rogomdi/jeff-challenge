package com.jeff.recommender.infrastructure.repository;

import com.jeff.recommender.domain.model.Recommendation;
import com.jeff.recommender.domain.repository.RecommendationRepository;
import com.jeff.recommender.infrastructure.mapper.MongoRecommendationMapper;
import com.jeff.recommender.infrastructure.model.RecommendationDocument;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class RecommendationRepositoryImpl implements RecommendationRepository {

  private final MongoRecommendationMapper recommendationMapper =
      Mappers.getMapper(MongoRecommendationMapper.class);
  private final SpringRecommendationRepository recommendationRepository;

  @Override
  public Optional<Recommendation> findById(UUID id) {
    return recommendationRepository.findById(id).map(recommendationMapper::toDomain);
  }

  @Override
  public Optional<Recommendation> findLastByCustomer(UUID customerId) {
    return recommendationRepository
        .findByCustomerId(
            customerId,
            PageRequest.of(0, 1)
                .withSort(
                    Sort.sort(RecommendationDocument.class)
                        .by(RecommendationDocument::getDateTime)
                        .descending()))
        .map(recommendationMapper::toDomain)
        .get()
        .findFirst();
  }

  @Override
  public Stream<Recommendation> findByCustomer(UUID customerId, int page, int size) {
    return recommendationRepository
        .findByCustomerId(
            customerId,
            PageRequest.of(page, size)
                .withSort(
                    Sort.sort(RecommendationDocument.class)
                        .by(RecommendationDocument::getDateTime)
                        .descending()))
        .map(recommendationMapper::toDomain)
        .get();
  }

  @Override
  public Recommendation save(Recommendation recommendation) {
    return recommendationMapper.toDomain(
        recommendationRepository.save(recommendationMapper.fromDomain(recommendation)));
  }
}
