package com.jeff.recommender.domain.model;

import lombok.Data;

@Data
public class Product {

    private int id;

    private String name;

    private ProductType type;

    private Vertical vertical;

    private double price;

    private double margin;

}
