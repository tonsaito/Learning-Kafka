package com.appsdeveloperblog.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Objects;

@Configuration
public class KafkaConfig {


    private final static Integer TOPIC_REPLICATION_FACTOR=3;
    private final static Integer TOPIC_PARTITIONS=3;

    @Autowired
    private Environment environment;

    @Bean
    NewTopic newProductsEventsTopic(){
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("products.events.topic.name")))
                .replicas(TOPIC_REPLICATION_FACTOR)
                .partitions(TOPIC_PARTITIONS)
                .build();
    }



    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
