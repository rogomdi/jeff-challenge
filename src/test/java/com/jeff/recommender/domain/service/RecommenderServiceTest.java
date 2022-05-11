package com.jeff.recommender.domain.service;

import com.jeff.recommender.MockUtils;
import com.jeff.recommender.domain.exception.CustomerNotFoundException;
import com.jeff.recommender.domain.exception.RecommendationNotFoundException;
import com.jeff.recommender.domain.model.*;
import com.jeff.recommender.domain.repository.CustomerRepository;
import com.jeff.recommender.domain.repository.RecommendationRepository;
import com.jeff.recommender.domain.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Tag("Unit")
class RecommenderServiceTest {
  private static final RecommendationRepository recommendationRepository =
      Mockito.mock(RecommendationRepository.class);

  private static final ScoreRepository scoreRepository = Mockito.mock(ScoreRepository.class);

  private static final CustomerRepository customerRepository =
      Mockito.mock(CustomerRepository.class);

  private final RecommenderService recommenderService =
      new RecommenderService(customerRepository, scoreRepository, recommendationRepository);

  private static final Product laundryPlan = MockUtils.buildLaundryPlan();
  private static final Satisfaction laundryPlanSatisfaction =
      MockUtils.buildSatisfaction(laundryPlan, 7);
  private static final Product haircut = MockUtils.buildHaircut();
  private static final Satisfaction haircutSatisfaction = MockUtils.buildSatisfaction(haircut, 7);
  private static final Product fullBodyMassage = MockUtils.buildFullBodyMassage();
  private static final Satisfaction fullBodyMassageSatisfaction =
      MockUtils.buildSatisfaction(fullBodyMassage, 9);
  private static final Product headMassage = MockUtils.buildHeadMassage();
  private static final Satisfaction headMassageSatisfaction =
      MockUtils.buildSatisfaction(headMassage, 8);
  private static final Customer customer = MockUtils.buildCustomer(Collections.emptyList());

  @BeforeAll
  public static void setup() {
    Mockito.when(recommendationRepository.save(Mockito.any(Recommendation.class)))
        .then(i -> i.getArgument(0));
  }

  @BeforeEach
  public void resetMocks() {
    Mockito.reset(scoreRepository, customerRepository);
  }

  @Test
  void getFailsWhenNotFound() {
    assertThrows(
        RecommendationNotFoundException.class, () -> recommenderService.get(UUID.randomUUID()));
  }

  @Test
  void get() {
    var recommendation =
        MockUtils.buildRecommendation(customer.getId(), fullBodyMassage, Type.VERTICAL, 9);
    Mockito.when(recommendationRepository.findById(Mockito.eq(recommendation.getId())))
        .thenReturn(Optional.of(recommendation));
    assertEquals(recommendation, recommenderService.get(recommendation.getId()));
  }

  @Test
  void findByCustomer() {
    var recommendations =
        List.of(
            MockUtils.buildRecommendation(customer.getId(), fullBodyMassage, Type.VERTICAL, 9),
            MockUtils.buildRecommendation(customer.getId(), haircut, Type.VERTICAL, 8));
    Mockito.when(
            recommendationRepository.findByCustomer(
                Mockito.eq(customer.getId()), Mockito.eq(0), Mockito.eq(10)))
        .thenReturn(recommendations.stream());
    assertEquals(
        recommendations.size(),
        (int) recommenderService.findByCustomer(customer.getId(), 0, 10).count());
  }

  @Test
  void recommendWhenOnlyVerticalRecommendation() {
    customer.setSubscriptions(List.of(laundryPlan, haircut, headMassage));
    Mockito.when(customerRepository.findById(Mockito.eq(customer.getId())))
        .thenReturn(Optional.of(customer));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(1)))
        .thenReturn(Optional.of(laundryPlanSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(3)))
        .thenReturn(Optional.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(6)))
        .thenReturn(Optional.of(fullBodyMassageSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(7)))
        .thenReturn(Optional.of(headMassageSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByPersona(Mockito.eq(Persona.SENIOR)))
        .thenReturn(List.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByVertical(Mockito.eq(Vertical.RELAX)))
        .thenReturn(List.of(fullBodyMassageSatisfaction, headMassageSatisfaction));
    List<Recommendation> recommendations =
        recommenderService.recommend(customer.getId()).collect(Collectors.toList());
    assertEquals(1, recommendations.size());
    assertEquals(fullBodyMassage, recommendations.get(0).getProduct());
    assertEquals(Type.VERTICAL, recommendations.get(0).getType());
  }

  @Test
  void recommendWhenOnlyTopProductsRecommendation() {
    customer.setSubscriptions(List.of(laundryPlan, fullBodyMassage, headMassage));
    Mockito.when(customerRepository.findById(Mockito.eq(customer.getId())))
        .thenReturn(Optional.of(customer));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(1)))
        .thenReturn(Optional.of(laundryPlanSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(3)))
        .thenReturn(Optional.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(6)))
        .thenReturn(Optional.of(fullBodyMassageSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(7)))
        .thenReturn(Optional.of(headMassageSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByPersona(Mockito.eq(Persona.SENIOR)))
        .thenReturn(List.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByVertical(Mockito.eq(Vertical.RELAX)))
        .thenReturn(List.of(fullBodyMassageSatisfaction, headMassageSatisfaction));
    List<Recommendation> recommendations =
        recommenderService.recommend(customer.getId()).collect(Collectors.toList());
    assertEquals(1, recommendations.size());
    assertEquals(haircut, recommendations.get(0).getProduct());
    assertEquals(Type.TOP_PRODUCTS, recommendations.get(0).getType());
  }

  @Test
  void recommendWhenBothRecommendations() {
    customer.setSubscriptions(List.of(laundryPlan, headMassage));
    Mockito.when(customerRepository.findById(Mockito.eq(customer.getId())))
        .thenReturn(Optional.of(customer));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(1)))
        .thenReturn(Optional.of(laundryPlanSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(3)))
        .thenReturn(Optional.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(6)))
        .thenReturn(Optional.of(fullBodyMassageSatisfaction));
    Mockito.when(scoreRepository.findLast(Mockito.eq(Persona.SENIOR), Mockito.eq(7)))
        .thenReturn(Optional.of(headMassageSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByPersona(Mockito.eq(Persona.SENIOR)))
        .thenReturn(List.of(haircutSatisfaction));
    Mockito.when(scoreRepository.findTopScoresByVertical(Mockito.eq(Vertical.RELAX)))
        .thenReturn(List.of(fullBodyMassageSatisfaction, headMassageSatisfaction));
    List<Recommendation> recommendations =
        recommenderService.recommend(customer.getId()).collect(Collectors.toList());
    assertEquals(2, recommendations.size());
    Recommendation verticalRecommendation =
        recommendations.stream()
            .filter(recommendation -> recommendation.getType().equals(Type.VERTICAL))
            .findAny()
            .orElse(null);
    assertNotNull(verticalRecommendation);
    assertEquals(fullBodyMassage, verticalRecommendation.getProduct());
    Recommendation topProductsRecommendation =
        recommendations.stream()
            .filter(recommendation -> recommendation.getType().equals(Type.TOP_PRODUCTS))
            .findAny()
            .orElse(null);
    assertNotNull(topProductsRecommendation);
    assertEquals(haircut, topProductsRecommendation.getProduct());
  }

  @Test
  void recommendThrowExceptionWhenNoCustomer() {
    Mockito.when(customerRepository.findById(Mockito.any(UUID.class)))
        .thenThrow(new CustomerNotFoundException());
    assertThrows(
        CustomerNotFoundException.class, () -> recommenderService.recommend(customer.getId()));
  }
}
