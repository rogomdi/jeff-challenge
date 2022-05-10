package com.jeff.recommender.infrastructure.config;

import com.jeff.recommender.infrastructure.converter.ZonedDateTimeReadConverter;
import com.jeff.recommender.infrastructure.converter.ZonedDateTimeWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoConfiguration {

  @Bean
  public MongoCustomConversions mongoCustomConversions() {

    return new MongoCustomConversions(
        List.of(new ZonedDateTimeReadConverter(), new ZonedDateTimeWriteConverter()));
  }
}
