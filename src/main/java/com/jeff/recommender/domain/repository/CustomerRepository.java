package com.jeff.recommender.domain.repository;

import com.jeff.recommender.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Optional<Customer> findById(UUID id);

}
