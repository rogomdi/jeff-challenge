package com.jeff.recommender.infrastructure.repository;

import com.jeff.recommender.MockUtils;
import com.jeff.recommender.domain.model.Recommendation;
import com.jeff.recommender.domain.model.Type;
import com.jeff.recommender.domain.repository.RecommendationRepository;
import com.jeff.recommender.infrastructure.config.MongoConfiguration;
import com.jeff.recommender.infrastructure.converter.ZonedDateTimeReadConverter;
import com.jeff.recommender.infrastructure.converter.ZonedDateTimeWriteConverter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@Import({RecommendationRepositoryImpl.class, MongoConfiguration.class})
@Tag("Integration")
class RecommendationRepositoryImplTest {

  @Autowired private RecommendationRepository recommendationRepository;

  @Test
  void findById() {
    Recommendation recommendation =
        new Recommendation(
            UUID.randomUUID(),
            MockUtils.buildFullBodyMassage(),
            9,
            Type.TOP_PRODUCTS,
            ZonedDateTime.now());
    assumeTrue(recommendationRepository.save(recommendation) != null);
    var retrievedRecommendation = recommendationRepository.findById(recommendation.getId());
    assertTrue(retrievedRecommendation.isPresent());
    assertEquals(recommendation.getCustomerId(), retrievedRecommendation.get().getCustomerId());
    assertEquals(recommendation.getProduct(), retrievedRecommendation.get().getProduct());
  }

  @Test
  void findLastByCustomer() {
    UUID customerId = UUID.randomUUID();
    Recommendation lastRecommendation =
        new Recommendation(
            customerId,
            MockUtils.buildFullBodyMassage(),
            9,
            Type.TOP_PRODUCTS,
            ZonedDateTime.now());
    assumeTrue(
        recommendationRepository.save(
                new Recommendation(
                    customerId,
                    MockUtils.buildHeadMassage(),
                    9,
                    Type.VERTICAL,
                    ZonedDateTime.now().minusHours(1)))
            != null);
    assumeTrue(recommendationRepository.save(lastRecommendation) != null);
    var retrievedRecommendation =
        recommendationRepository.findLastByCustomer(customerId);
    assertTrue(retrievedRecommendation.isPresent());
    assertEquals(lastRecommendation.getCustomerId(), retrievedRecommendation.get().getCustomerId());
    assertEquals(lastRecommendation.getProduct(), retrievedRecommendation.get().getProduct());
    assertEquals(lastRecommendation.getType(), retrievedRecommendation.get().getType());
    assertEquals(lastRecommendation.getId(), retrievedRecommendation.get().getId());
  }

  @Test
  void findByCustomer() {
    UUID customerId = UUID.randomUUID();
    Recommendation lastRecommendation =
        new Recommendation(
            customerId,
            MockUtils.buildFullBodyMassage(),
            9,
            Type.TOP_PRODUCTS,
            ZonedDateTime.now());
    assumeTrue(
        recommendationRepository.save(
                new Recommendation(
                    customerId,
                    MockUtils.buildHeadMassage(),
                    9,
                    Type.VERTICAL,
                    ZonedDateTime.now().minusHours(1)))
            != null);
    assumeTrue(recommendationRepository.save(lastRecommendation) != null);
    var retrievedRecommendations =
        recommendationRepository.findByCustomer(customerId, 0, 10);
    assertEquals(2, retrievedRecommendations.count());
  }

  @Test
  void save() {
    Recommendation recommendation =
        new Recommendation(
            UUID.randomUUID(),
            MockUtils.buildFullBodyMassage(),
            9,
            Type.TOP_PRODUCTS,
            ZonedDateTime.now());
    recommendation = recommendationRepository.save(recommendation);
    assertNotNull(recommendation);
  }
}
