package com.jeff.recommender.domain.model;

import lombok.Data;

@Data
public class Satisfaction {

    private Product product;

    private Persona persona;

    private int score;
}
