package com.jeff.recommender.domain.exception;

public class CustomerNotFoundException extends RecommenderException {
    public CustomerNotFoundException(){
        super();
    }

    public CustomerNotFoundException(Throwable e){
        super(e);
    }

    public CustomerNotFoundException(String message){
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable e){
        super(message, e);
    }
}
