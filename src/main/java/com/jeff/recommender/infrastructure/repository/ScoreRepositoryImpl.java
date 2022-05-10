package com.jeff.recommender.infrastructure.repository;

import com.jeff.recommender.domain.model.Persona;
import com.jeff.recommender.domain.model.Satisfaction;
import com.jeff.recommender.domain.model.Vertical;
import com.jeff.recommender.domain.repository.ScoreRepository;
import com.jeff.recommender.infrastructure.config.RecommenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScoreRepositoryImpl implements ScoreRepository {

  private static final String SCORE_SERVICE_NOT_AVAILABLE = "Score service not available";
  private final RestTemplate restTemplate = new RestTemplate();

  private final CircuitBreaker scoreCircuitBreaker;

  private final RecommenderProperties recommenderProperties;

  @Override
  public Optional<Satisfaction> findLast(Persona persona, int productId) {
    return scoreCircuitBreaker.run(
        () ->
            Optional.ofNullable(
                restTemplate.getForObject(
                    recommenderProperties.getScoreServiceUrl()
                        + "?persona={persona}&product={product}",
                    Satisfaction.class,
                    Map.of("persona", persona, "product", productId))),
        t -> {
          log.warn(SCORE_SERVICE_NOT_AVAILABLE, t);
          return Optional.empty();
        });
  }

  @Override
  public List<Satisfaction> findTopScoresByPersona(Persona persona) {
    return findAllProductsScores(persona).collect(Collectors.toList());
  }

  @Override
  public List<Satisfaction> findTopScoresByVertical(Vertical vertical) {
    return Stream.concat(
            findAllProductsScores(Persona.SENIOR), findAllProductsScores(Persona.JUNIOR))
        .filter(satisfaction -> satisfaction.getProduct().getVertical() == vertical)
        .collect(Collectors.toList());
  }

  private Stream<Satisfaction> findAllProductsScores(Persona persona) {
    // Since the challenge description says that we have the endpoint and a subset of 7 products, we
    // make one call for every product. It would be better to have and endpoint to retrieve all of
    // them in a single call
    return Stream.of(1, 2, 3, 4, 5, 6, 7)
        .parallel()
        .map(
            productId ->
                scoreCircuitBreaker.run(
                    () ->
                        Optional.ofNullable(
                            restTemplate.getForObject(
                                recommenderProperties.getScoreServiceUrl()
                                    + "?persona={persona}&product={product}",
                                Satisfaction.class,
                                Map.of("persona", persona, "product", productId))),
                    t -> {
                      log.warn(SCORE_SERVICE_NOT_AVAILABLE, t);
                      return Optional.empty();
                    }))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(s -> (Satisfaction) s);
  }
}
