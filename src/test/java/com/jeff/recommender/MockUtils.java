package com.jeff.recommender;

import com.jeff.recommender.domain.model.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class MockUtils {

    public static Recommendation buildRecommendation(UUID customerId, Product product, Type type, int score){
        Recommendation recommendation = new Recommendation();
        recommendation.setId(UUID.randomUUID());
        recommendation.setProduct(product);
        recommendation.setCustomerId(customerId);
        recommendation.setScore(score);
        recommendation.setType(type);
        recommendation.setDateTime(ZonedDateTime.now());
        return recommendation;
    }
    public static Satisfaction buildSatisfaction(Product product, int score) {
        Satisfaction fullBodyMassageSatisfaction = new Satisfaction();
        fullBodyMassageSatisfaction.setPersona(Persona.SENIOR);
        fullBodyMassageSatisfaction.setProduct(product);
        fullBodyMassageSatisfaction.setScore(score);
        return fullBodyMassageSatisfaction;
    }

    public static Product buildLaundryPlan() {
        Product laundryPlan = new Product();
        laundryPlan.setId(1);
        laundryPlan.setVertical(Vertical.LAUNDRY);
        laundryPlan.setName("Laundry Plan S");
        laundryPlan.setType(ProductType.SUBSCRIPTION);
        laundryPlan.setPrice(54.99);
        laundryPlan.setMargin(16.49);
        return laundryPlan;
    }

    public static Product buildHaircut() {
        Product haircut = new Product();
        haircut.setId(3);
        haircut.setVertical(Vertical.BEAUTY);
        haircut.setName("Haircut");
        haircut.setType(ProductType.SERVICE);
        haircut.setPrice(9.95);
        haircut.setMargin(1.99);
        return haircut;
    }

    public static Product buildFullBodyMassage() {
        Product fullBodyMassage = new Product();
        fullBodyMassage.setId(6);
        fullBodyMassage.setVertical(Vertical.RELAX);
        fullBodyMassage.setName("Full Body Massage");
        fullBodyMassage.setType(ProductType.SERVICE);
        fullBodyMassage.setPrice(9.99);
        fullBodyMassage.setMargin(2.99);
        return fullBodyMassage;
    }

    public static Product buildHeadMassage() {
        Product headMassage = new Product();
        headMassage.setId(7);
        headMassage.setVertical(Vertical.RELAX);
        headMassage.setName("Head Massage");
        headMassage.setType(ProductType.SERVICE);
        headMassage.setPrice(5.99);
        headMassage.setMargin(1.19);
        return headMassage;
    }

    public static Customer buildCustomer(List<Product> products) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("test user");
        customer.setPersona(Persona.SENIOR);
        customer.setSubscriptions(products);
        return customer;
    }
}