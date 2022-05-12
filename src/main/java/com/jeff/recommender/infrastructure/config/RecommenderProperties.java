package com.jeff.recommender.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.Document;

@Configuration
@ConfigurationProperties(prefix = "recommender")
@Data
public class RecommenderProperties {

    /**
     * URL for the score service
     */
    private String scoreServiceUrl = "http://localhost:9001/scores";

    /**
     * URL for the score service
     */
    private String customerServiceUrl = "http://localhost:9002/customers";

    public String getCustomerServiceUrl() {
        return customerServiceUrl.endsWith("/") ? customerServiceUrl : customerServiceUrl + "/";
    }

    public String getScoreServiceUrl() {
        return scoreServiceUrl.endsWith("/") ? scoreServiceUrl : scoreServiceUrl + "/";
    }
}
