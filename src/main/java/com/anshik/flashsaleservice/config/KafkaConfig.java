package com.anshik.flashsaleservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic flashSaleOrdersTopic() {
        return TopicBuilder.name("flash-sale-orders")
                .partitions(3)
                .replicas(3)
                .build();
    }
}