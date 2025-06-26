package com.example.allrealmen.config;

import com.example.allrealmen.config.converter.DateToLocalDateTimeKstConverter;
import com.example.allrealmen.config.converter.LocalDateTimeToDateKstConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    @Bean
    public MongoCustomConversions customConversions(
            LocalDateTimeToDateKstConverter localDateTimeToDateKstConverter,
            DateToLocalDateTimeKstConverter dateToLocalDateTimeKstConverter
    ) {
        return new MongoCustomConversions(Arrays.asList(
                localDateTimeToDateKstConverter,
                dateToLocalDateTimeKstConverter
        ));
    }
} 