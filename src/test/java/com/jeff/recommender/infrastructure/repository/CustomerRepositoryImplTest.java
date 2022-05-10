package com.jeff.recommender.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jeff.recommender.MockUtils;
import com.jeff.recommender.domain.model.Customer;
import com.jeff.recommender.domain.model.Persona;
import com.jeff.recommender.domain.model.Satisfaction;
import com.jeff.recommender.domain.repository.CustomerRepository;
import com.jeff.recommender.domain.repository.ScoreRepository;
import com.jeff.recommender.infrastructure.config.RecommenderConfiguration;
import com.jeff.recommender.infrastructure.config.RecommenderProperties;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import({CustomerRepositoryImpl.class, RecommenderConfiguration.class, RecommenderProperties.class})
@ImportAutoConfiguration({
        Resilience4JAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class,
        TimeLimiterAutoConfiguration.class
})
@Tag("Integration")
class CustomerRepositoryImplTest {

    @Autowired
    CustomerRepository customerRepository;

    @RegisterExtension
    static WireMockExtension wireMockExtension =
            WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    public static void setupUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "recommender.customer-service-url",
                () -> wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + "/customer/");
    }

    @Test
    void findById() throws JsonProcessingException {
        Customer customer = MockUtils.buildCustomer(List.of(MockUtils.buildFullBodyMassage()));
        wireMockExtension
                .getRuntimeInfo()
                .getWireMock()
                .register(
                        WireMock.get("/customer/" + customer.getId())
                                .willReturn(
                                        ok().withBody(new ObjectMapper().writeValueAsString(customer))
                                                .withHeader("Content-Type", "application/json;charset=UTF-8")));
        Optional<Customer> returnedCustomer = customerRepository.findById(customer.getId());
        assertTrue(returnedCustomer.isPresent());
        assertEquals(customer, returnedCustomer.get());
    }
}