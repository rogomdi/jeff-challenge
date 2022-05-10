package com.jeff.recommender.domain.exception;

public class RecommenderException extends RuntimeException {

    public RecommenderException(){
        super();
    }

    public RecommenderException(Throwable e){
        super(e);
    }

    public RecommenderException(String message){
        super(message);
    }

    public RecommenderException(String message, Throwable e){
        super(message, e);
    }

}
