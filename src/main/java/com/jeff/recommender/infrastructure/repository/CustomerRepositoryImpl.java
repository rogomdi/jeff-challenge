package com.jeff.recommender.infrastructure.repository;

import com.jeff.recommender.domain.model.Customer;
import com.jeff.recommender.domain.repository.CustomerRepository;
import com.jeff.recommender.infrastructure.config.RecommenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerRepositoryImpl implements CustomerRepository {

  private final RestTemplate restTemplate = new RestTemplate();

  private final CircuitBreaker customerCircuitBreaker;

  private final RecommenderProperties recommenderProperties;

  @Override
  public Optional<Customer> findById(UUID id) {
    return customerCircuitBreaker.run(
        () ->
            Optional.ofNullable(
                restTemplate.getForObject(
                    recommenderProperties.getCustomerServiceUrl() + id, Customer.class)),
        t -> {
          log.warn("Customer service not available", t);
          return Optional.empty();
        });
  }
}
