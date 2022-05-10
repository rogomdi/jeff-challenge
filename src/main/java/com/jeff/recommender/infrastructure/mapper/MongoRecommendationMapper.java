package com.jeff.recommender.infrastructure.mapper;

import com.jeff.recommender.domain.model.Recommendation;
import com.jeff.recommender.infrastructure.model.RecommendationDocument;
import org.mapstruct.Mapper;

@Mapper
public interface MongoRecommendationMapper  {

    Recommendation toDomain(RecommendationDocument recommendationDocument);
    RecommendationDocument fromDomain(Recommendation recommendation);
}
