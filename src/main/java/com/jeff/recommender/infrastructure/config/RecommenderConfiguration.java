package com.jeff.recommender.infrastructure.config;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommenderConfiguration {

  @Bean
  public CircuitBreaker customerCircuitBreaker(CircuitBreakerFactory circuitBreakerFactory) {
    return circuitBreakerFactory.create("customerCircuitBreaker");
  }

  @Bean
  public CircuitBreaker scoreCircuitBreaker(CircuitBreakerFactory circuitBreakerFactory) {
    return circuitBreakerFactory.create("scoreCircuitBreaker");
  }
}
