package com.jeff.recommender.application.graphql.mapper;

import com.jeff.recommender.application.graphql.types.Recommendation;
import org.mapstruct.Mapper;

@Mapper
public interface GraphqlRecommendationMapper {

  Recommendation fromDomain(com.jeff.recommender.domain.model.Recommendation recommendation);

  com.jeff.recommender.domain.model.Recommendation toDomain(Recommendation recommendation);
}
