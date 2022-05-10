package com.jeff.recommender.domain.model;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Customer {

    private UUID id;

    private String name;

    private Persona persona;

    private List<Product> subscriptions;

}
