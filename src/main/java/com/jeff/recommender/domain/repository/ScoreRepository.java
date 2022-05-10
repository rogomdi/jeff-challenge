package com.jeff.recommender.domain.repository;

import com.jeff.recommender.domain.model.Persona;
import com.jeff.recommender.domain.model.Satisfaction;
import com.jeff.recommender.domain.model.Vertical;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ScoreRepository {

    Optional<Satisfaction> findLast(Persona persona, int productId);

    List<Satisfaction> findTopScoresByPersona(Persona persona);

    List<Satisfaction> findTopScoresByVertical(Vertical vertical);

}
