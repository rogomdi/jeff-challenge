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

  /**
   * Generates a recommendation for the {@link Customer}
   *
   * @param customerId {@link Customer} id
   * @return {@link Stream} with the generated recommendations
   */
  public Stream<Recommendation> recommend(UUID customerId) {
    Customer customer =
        customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
    ZonedDateTime generationTime = ZonedDateTime.now();
    return Stream.concat(
        getVerticalRecommendation(customer, getTopScoredProduct(customer), generationTime),
        getTopProductsRecommendations(customer.getPersona(), customer, generationTime));
  }

  /**
   * Gets the product with highest score among the customer subscriptions
   *
   * @param customer {@link Customer} to generate the recommendation for
   * @return The {@link Satisfaction} for the {@link Product} with highest score
   */
  private Optional<Satisfaction> getTopScoredProduct(Customer customer) {
    return customer.getSubscriptions().stream()
        .map(product -> scoreRepository.findLast(customer.getPersona(), product.getId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .max(Comparator.comparing(Satisfaction::getScore, Comparator.naturalOrder()));
  }

  /**
   * Generates a series of {@link Recommendation} based on the top scored products in every vertical
   *
   * @param customer {@link Customer} to generate the recommendation for
   * @param topScoredProduct The {@link Satisfaction} for the {@link Product} with highest score
   * @param generationTime time when the recommendation started
   * @return {@link Stream} with the generated recommendations
   */
  private Stream<Recommendation> getVerticalRecommendation(
      Customer customer, Optional<Satisfaction> topScoredProduct, ZonedDateTime generationTime) {
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

  /**
   * Generates a series of {@link Recommendation} based on the top scored products for the customer
   * {@link Persona}
   *
   * @param persona {@link Persona} to generate the recommendation for
   * @param customer {@link Customer} to generate the recommendation for
   * @param generationTime time when the recommendation started
   * @return {@link Stream} with the generated recommendations
   */
  private Stream<Recommendation> getTopProductsRecommendations(
      Persona persona, Customer customer, ZonedDateTime generationTime) {
    return buildRecommendation(
        scoreRepository.findTopScoresByPersona(persona).stream(),
        customer,
        Type.TOP_PRODUCTS,
        generationTime);
  }

  /**
   * Transforms the {@link Stream} of {@link Satisfaction} into an {@link Stream} of {@link
   * Recommendation} excluding the products that the {@link Customer} is subscribed already
   *
   * @param scores {@link Stream} of {@link Satisfaction} generated
   * @param customer {@link Customer} to generate the recommendation for
   * @param recommendationType {@link Type} of {@link Recommendation}
   * @param generationTime time when the recommendation started
   * @return {@link Stream} with the generated recommendations
   */
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
