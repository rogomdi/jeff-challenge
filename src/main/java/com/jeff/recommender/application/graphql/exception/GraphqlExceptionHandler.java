package com.jeff.recommender.application.graphql.exception;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphqlExceptionHandler implements DataFetcherExceptionHandler {

  @Override
  public DataFetcherExceptionHandlerResult onException(
      DataFetcherExceptionHandlerParameters handlerParameters) {
    return DataFetcherExceptionHandlerResult.newResult()
        .error(
            ApplicationError.getFromDomainException(
                handlerParameters.getException(), Map.of(), handlerParameters.getPath()))
        .build();
  }
}
