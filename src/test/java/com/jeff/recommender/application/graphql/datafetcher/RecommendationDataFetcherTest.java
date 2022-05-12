package com.jeff.recommender.application.graphql.datafetcher;

import com.jeff.recommender.MockUtils;
import com.jeff.recommender.application.graphql.exception.ApplicationError;
import com.jeff.recommender.application.graphql.exception.GraphqlExceptionHandler;
import com.jeff.recommender.domain.exception.CustomerNotFoundException;
import com.jeff.recommender.domain.exception.RecommendationNotFoundException;
import com.jeff.recommender.domain.model.Customer;
import com.jeff.recommender.domain.model.Recommendation;
import com.jeff.recommender.domain.model.Type;
import com.jeff.recommender.domain.service.RecommenderService;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration;
import com.netflix.graphql.types.errors.ErrorType;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = {
      DgsAutoConfiguration.class,
      RecommendationDataFetcher.class,
      DgsExtendedScalarsAutoConfiguration.class,
      GraphqlExceptionHandler.class
    })
@Tag("Integration")
class RecommendationDataFetcherTest {

  @MockBean RecommenderService recommenderService;

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @BeforeEach
  public void reset(){
    Mockito.reset(recommenderService);
  }

  @Test
  void recommendation() {
    Customer customer = MockUtils.buildCustomer(List.of(MockUtils.buildFullBodyMassage()));
    Recommendation recommendation =
        MockUtils.buildRecommendation(
            customer.getId(), MockUtils.buildHaircut(), Type.TOP_PRODUCTS, 8);
    Mockito.when(recommenderService.get(Mockito.eq(recommendation.getId())))
        .thenReturn(recommendation);
    Recommendation result =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            "query { recommendation(id:\""
                + recommendation.getId().toString()
                + "\") {id, customerId, type} }",
            "data.recommendation",
            Recommendation.class);
    assertEquals(recommendation.getId(), result.getId());
    assertEquals(recommendation.getCustomerId(), result.getCustomerId());
    assertEquals(recommendation.getType(), result.getType());
  }
  @Test
  void recommendationWhenNotFound() {
    Mockito.when(recommenderService.get(Mockito.any()))
        .thenThrow(new RecommendationNotFoundException());
    var result =
        dgsQueryExecutor.execute(
            "query { recommendation(id:\""
                + UUID.randomUUID()
                + "\") {id, customerId, type} }");
    assertEquals(ErrorType.NOT_FOUND.name(), result.getErrors().get(0).getExtensions().get("errorType"));
    assertEquals(ApplicationError.RECOMMENDATION_NOT_FOUND.getError().build().getMessage(), result.getErrors().get(0).getMessage());
  }

  @Test
  void recommendationsWhenDefaultPaging() {
    Customer customer = MockUtils.buildCustomer(List.of(MockUtils.buildFullBodyMassage()));
    Recommendation recommendation =
        MockUtils.buildRecommendation(
            customer.getId(), MockUtils.buildHaircut(), Type.TOP_PRODUCTS, 8);
    ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
    Mockito.when(
            recommenderService.findByCustomer(
                Mockito.eq(customer.getId()), pageCaptor.capture(), sizeCaptor.capture()))
        .thenReturn(Stream.of(recommendation));
    List<Recommendation> result =
        Arrays.stream(
                dgsQueryExecutor.executeAndExtractJsonPathAsObject(
                    "query { recommendations(customerId:\""
                        + customer.getId().toString()
                        + "\") {id, customerId, type} }",
                    "data.recommendations",
                    Recommendation[].class))
            .collect(Collectors.toList());
    assertEquals(0, pageCaptor.getValue());
    assertEquals(10, sizeCaptor.getValue());
    assertEquals(1, result.size());
    assertEquals(recommendation.getId(), result.get(0).getId());
    assertEquals(recommendation.getCustomerId(), result.get(0).getCustomerId());
    assertEquals(recommendation.getType(), result.get(0).getType());
  }

  @Test
  void recommendationsWhenCustomPaging() {
    Customer customer = MockUtils.buildCustomer(List.of(MockUtils.buildFullBodyMassage()));
    Recommendation recommendation =
        MockUtils.buildRecommendation(
            customer.getId(), MockUtils.buildHaircut(), Type.TOP_PRODUCTS, 8);
    ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
    Mockito.when(
            recommenderService.findByCustomer(
                Mockito.eq(customer.getId()), pageCaptor.capture(), sizeCaptor.capture()))
        .thenReturn(Stream.of(recommendation));
    int page = 1;
    int size = 5;
    List<Recommendation> result =
        Arrays.stream(
                dgsQueryExecutor.executeAndExtractJsonPathAsObject(
                    "query { recommendations(customerId:\""
                        + customer.getId().toString()
                        + "\", paging: {page:"
                        + page
                        + ", size:"
                        + size
                        + "}) {id, customerId, type} }",
                    "data.recommendations",
                    Recommendation[].class))
            .collect(Collectors.toList());
    assertEquals(page, pageCaptor.getValue());
    assertEquals(size, sizeCaptor.getValue());
    assertEquals(1, result.size());
    assertEquals(recommendation.getId(), result.get(0).getId());
    assertEquals(recommendation.getCustomerId(), result.get(0).getCustomerId());
    assertEquals(recommendation.getType(), result.get(0).getType());
  }

  @Test
  void recommend() {
    Customer customer = MockUtils.buildCustomer(List.of(MockUtils.buildFullBodyMassage()));
    Recommendation recommendation =
        MockUtils.buildRecommendation(
            customer.getId(), MockUtils.buildHaircut(), Type.TOP_PRODUCTS, 8);
    Mockito.when(recommenderService.recommend(Mockito.eq(customer.getId())))
        .thenReturn(Stream.of(recommendation));
    List<Recommendation> result =
        Arrays.stream(
                dgsQueryExecutor.executeAndExtractJsonPathAsObject(
                    "mutation { recommend(customerId:\""
                        + customer.getId().toString()
                        + "\") {id, customerId, type} }",
                    "data.recommend",
                    Recommendation[].class))
            .collect(Collectors.toList());
    assertEquals(1, result.size());
    assertEquals(recommendation.getId(), result.get(0).getId());
  }

  @Test
  void recommendWhenCustomerNotFound() {
    Mockito.when(recommenderService.recommend(Mockito.any()))
        .thenThrow(new CustomerNotFoundException());
    ExecutionResult result =
                dgsQueryExecutor.execute(
                    "mutation { recommend(customerId:\""
                        + UUID.randomUUID()
                        + "\") {id, customerId, type} }");
    assertEquals(ErrorType.NOT_FOUND.name(), result.getErrors().get(0).getExtensions().get("errorType"));
    assertEquals(ApplicationError.CUSTOMER_NOT_FOUND.getError().build().getMessage(), result.getErrors().get(0).getMessage());
  }

  @Test
  void recommendWhenUnknownError() {
    Mockito.when(recommenderService.recommend(Mockito.any()))
        .thenThrow(new RuntimeException());
    ExecutionResult result =
                dgsQueryExecutor.execute(
                    "mutation { recommend(customerId:\""
                        + UUID.randomUUID()
                        + "\") {id, customerId, type} }");
    assertEquals(ErrorType.INTERNAL.name(), result.getErrors().get(0).getExtensions().get("errorType"));
    assertEquals(ApplicationError.INTERNAL_ERROR.getError().build().getMessage(), result.getErrors().get(0).getMessage());
  }
}
