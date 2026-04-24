package com.anshik.flashsaleservice.config;

import com.anshik.flashsaleservice.dto.OrderEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;

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