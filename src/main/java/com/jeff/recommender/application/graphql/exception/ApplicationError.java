package com.jeff.recommender.application.graphql.exception;

import com.jeff.recommender.domain.exception.CustomerNotFoundException;
import com.jeff.recommender.domain.exception.RecommendationNotFoundException;
import com.jeff.recommender.domain.exception.RecommenderException;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ResultPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum ApplicationError {
  CUSTOMER_NOT_FOUND(
      CustomerNotFoundException.class,
      TypedGraphQLError.newNotFoundBuilder().message("Customer not found")),
  RECOMMENDATION_NOT_FOUND(
      RecommendationNotFoundException.class,
      TypedGraphQLError.newNotFoundBuilder().message("Recommendation not found")),
  INTERNAL_ERROR(
      RecommenderException.class,
      TypedGraphQLError.newInternalErrorBuilder().message("Something went wrong"));

  private final Class<?> exceptionClass;
  private final TypedGraphQLError.Builder error;

  /**
   * Get GraphQL error from the domain exception
   *
   * @param exception Domain exception thrown
   * @param debugInfo Extra info to add in the GraphQL error
   * @param path GraphQL path where the error happened
   * @return GraphQL error
   */
  public static TypedGraphQLError getFromDomainException(
      Throwable exception, Map<String, Object> debugInfo, ResultPath path) {
    return Arrays.stream(ApplicationError.values())
        .filter(error -> error.getExceptionClass().equals(exception.getClass()))
        .map(ApplicationError::getError)
        .findFirst()
        .orElse(INTERNAL_ERROR.getError())
        .debugInfo(debugInfo)
        .path(path)
        .build();
  }
}
