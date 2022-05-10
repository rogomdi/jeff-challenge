package com.jeff.recommender.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.jeff.recommender.MockUtils;
import com.jeff.recommender.domain.model.Persona;
import com.jeff.recommender.domain.model.Satisfaction;
import com.jeff.recommender.domain.model.Vertical;
import com.jeff.recommender.domain.repository.ScoreRepository;
import com.jeff.recommender.infrastructure.config.RecommenderConfiguration;
import com.jeff.recommender.infrastructure.config.RecommenderProperties;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import({ScoreRepositoryImpl.class, RecommenderConfiguration.class, RecommenderProperties.class})
@ImportAutoConfiguration({
  Resilience4JAutoConfiguration.class,
  CircuitBreakerAutoConfiguration.class,
  TimeLimiterAutoConfiguration.class
})
@Tag("Integration")
class ScoreRepositoryImplTest {

  @Autowired ScoreRepository scoreRepository;

  @RegisterExtension
  static WireMockExtension wireMockExtension =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  public static void setupUrl(DynamicPropertyRegistry registry) {
    registry.add(
        "recommender.score-service-url",
        () -> wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + "/score/");
  }

  @Test
  void findLast() throws JsonProcessingException {
    var satisfaction = MockUtils.buildSatisfaction(MockUtils.buildLaundryPlan(), 7);
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(
                    ok().withBody(new ObjectMapper().writeValueAsString(satisfaction))
                        .withHeader("Content-Type", "application/json;charset=UTF-8")));
    Optional<Satisfaction> returnedSatisfaction =
        scoreRepository.findLast(Persona.SENIOR, satisfaction.getProduct().getId());
    assertTrue(returnedSatisfaction.isPresent());
    assertEquals(satisfaction, returnedSatisfaction.get());
  }

  @Test
  void findLastWhenNotAnswering() {
    var satisfaction = MockUtils.buildSatisfaction(MockUtils.buildLaundryPlan(), 7);
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(serviceUnavailable()));
    assertTrue(
        scoreRepository.findLast(Persona.SENIOR, satisfaction.getProduct().getId()).isEmpty());
  }

  @Test
  void findTopScoresByPersona() throws JsonProcessingException {
    var satisfaction = MockUtils.buildSatisfaction(MockUtils.buildLaundryPlan(), 7);
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(
                    ok().withBody(new ObjectMapper().writeValueAsString(satisfaction))
                        .withHeader("Content-Type", "application/json;charset=UTF-8")));
    List<Satisfaction> returnedSatisfaction =
        scoreRepository.findTopScoresByPersona(Persona.SENIOR);
    assertEquals(1, returnedSatisfaction.size());
  }

  @Test
  void findTopScoresByPersonaWhenNotAnswering() {
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(serviceUnavailable()));
    List<Satisfaction> returnedSatisfaction =
        scoreRepository.findTopScoresByPersona(Persona.SENIOR);
    assertEquals(0, returnedSatisfaction.size());
  }

  @Test
  void findTopScoresByVertical() throws JsonProcessingException {
    var satisfaction = MockUtils.buildSatisfaction(MockUtils.buildLaundryPlan(), 7);
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(
                    ok().withBody(new ObjectMapper().writeValueAsString(satisfaction))
                        .withHeader("Content-Type", "application/json;charset=UTF-8")));
    List<Satisfaction> returnedSatisfaction =
        scoreRepository.findTopScoresByVertical(Vertical.LAUNDRY);
    assertEquals(1, returnedSatisfaction.size());
  }

  @Test
  void findTopScoresByVerticalWhenNotAnswering() {
    wireMockExtension
        .getRuntimeInfo()
        .getWireMock()
        .register(
            WireMock.get("/score/?persona=" + Persona.SENIOR + "&product=" + 1)
                .willReturn(serviceUnavailable()));
    List<Satisfaction> returnedSatisfaction =
        scoreRepository.findTopScoresByVertical(Vertical.LAUNDRY);
    assertEquals(0, returnedSatisfaction.size());
  }
}
