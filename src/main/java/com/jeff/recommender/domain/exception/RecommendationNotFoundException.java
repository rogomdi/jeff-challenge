package com.jeff.recommender.domain.exception;

public class RecommendationNotFoundException extends RecommenderException {
    public RecommendationNotFoundException(){
        super();
    }

    public RecommendationNotFoundException(Throwable e){
        super(e);
    }

    public RecommendationNotFoundException(String message){
        super(message);
    }

    public RecommendationNotFoundException(String message, Throwable e){
        super(message, e);
    }
}
