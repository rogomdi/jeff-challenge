package com.jeff.recommender.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Recommendation {

    private UUID id = UUID.randomUUID();

    @NonNull
    private UUID customerId;

    @NonNull
    private Product product;

    @NonNull
    private int score;

    @NonNull
    private Type type;

    @NonNull
    private ZonedDateTime dateTime;

}
