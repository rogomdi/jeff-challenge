package com.jeff.recommender.domain.service;

import com.jeff.recommender.domain.exception.CustomerNotFoundException;
import com.jeff.recommender.domain.exception.RecommendationNotFoundException;
import com.jeff.recommender.domain.model.*;
import com.jeff.recommender.domain.repository.CustomerRepository;
import com.jeff.recommender.domain.repository.RecommendationRepository;
import com.jeff.recommender.domain.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RecommenderService {

  private final CustomerRepository customerRepository;

  private final ScoreRepository scoreRepository;

  private final RecommendationRepository recommendationRepository;

  public Recommendation get(UUID id) {
    return recommendationRepository.findById(id).orElseThrow(RecommendationNotFoundException::new);
  }

  public Stream<Recommendation> findByCustomer(UUID customerId, int page, int size) {
    return recommendationRepository.findByCustomer(customerId, page, size);
  }
  public List<Recommendation> recommend(UUID customerId) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(CustomerNotFoundException::new);
    ZonedDateTime generationTime = ZonedDateTime.now();
    return Stream.concat(
            getVerticalRecommendation(customer, getTopScoredProduct(customer), generationTime),
            getTopProductsRecommendations(customer.getPersona(), customer, generationTime))
        .collect(Collectors.toList());
  }

  private Optional<Satisfaction> getTopScoredProduct(Customer customer) {
    return customer.getSubscriptions().stream()
        .map(product -> scoreRepository.findLast(customer.getPersona(), product.getId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .max(Comparator.comparing(Satisfaction::getScore, Comparator.naturalOrder()));
  }

  private Stream<Recommendation> getVerticalRecommendation(
      Customer customer, Optional<Satisfaction> topScoredProduct, ZonedDateTime generationTime) {
    // TODO Make size dynamic depending on the number of products on every vertical
    return topScoredProduct
        .map(
            satisfaction ->
                buildRecommendation(
                    scoreRepository
                        .findTopScoresByVertical(satisfaction.getProduct().getVertical())
                        .stream(),
                    customer,
                    Type.VERTICAL,
                    generationTime))
        .orElse(Stream.empty());
  }

  private Stream<Recommendation> getTopProductsRecommendations(
      Persona persona, Customer customer, ZonedDateTime generationTime) {
    return buildRecommendation(
        scoreRepository.findTopScoresByPersona(persona).stream(),
        customer,
        Type.TOP_PRODUCTS,
        generationTime);
  }

  private Stream<Recommendation> buildRecommendation(
      Stream<Satisfaction> scores,
      Customer customer,
      Type recommendationType,
      ZonedDateTime generationTime) {
    return scores
        .sorted(Comparator.comparing(Satisfaction::getScore, Comparator.reverseOrder()))
        .filter(satisfaction -> !isSubscribed(customer.getSubscriptions(), satisfaction))
        .map(
            satisfaction ->
                recommendationRepository.save(
                    new Recommendation(
                        customer.getId(),
                        satisfaction.getProduct(),
                        satisfaction.getScore(),
                        recommendationType,
                        generationTime)));
  }

  private boolean isSubscribed(List<Product> customerProducts, Satisfaction satisfaction) {
    return customerProducts.stream()
        .anyMatch(product -> product.getId() == satisfaction.getProduct().getId());
  }
}
