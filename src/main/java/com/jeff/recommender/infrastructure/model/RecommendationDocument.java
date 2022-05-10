package com.jeff.recommender.infrastructure.model;

import com.jeff.recommender.domain.model.Product;
import com.jeff.recommender.domain.model.Type;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "recommendation")
public class RecommendationDocument {

  @Id private UUID id;

  private UUID customerId;

  // To facilitate the work we embed the product into the document since we don't have an API or DB
  // to retrieve products, but this should be an ID as in the customer
  private Product product;

  private int score;

  private Type type;

  private ZonedDateTime dateTime;
}
