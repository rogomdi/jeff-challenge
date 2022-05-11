package com.jeff.recommender.application.graphql.datafetcher;

import com.jeff.recommender.application.graphql.mapper.GraphqlRecommendationMapper;
import com.jeff.recommender.application.graphql.types.Paging;
import com.jeff.recommender.application.graphql.types.Recommendation;
import com.jeff.recommender.domain.service.RecommenderService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class RecommendationDataFetcher {

  private final RecommenderService recommenderService;
  private final GraphqlRecommendationMapper graphqlRecommendationMapper =
      Mappers.getMapper(GraphqlRecommendationMapper.class);

  @DgsQuery
  public Recommendation recommendation(@InputArgument UUID id) {
    return graphqlRecommendationMapper.fromDomain(recommenderService.get(id));
  }

  @DgsQuery
  public List<Recommendation> recommendations(
      @InputArgument UUID customerId, @InputArgument Paging paging) {
    return recommenderService
        .findByCustomer(customerId, paging.getPage(), paging.getSize())
        .map(graphqlRecommendationMapper::fromDomain)
        .collect(Collectors.toList());
  }

  @DgsMutation
  public List<Recommendation> recommend(@InputArgument UUID customerId) {
    return recommenderService
        .recommend(customerId)
        .map(graphqlRecommendationMapper::fromDomain)
        .collect(Collectors.toList());
  }
}
