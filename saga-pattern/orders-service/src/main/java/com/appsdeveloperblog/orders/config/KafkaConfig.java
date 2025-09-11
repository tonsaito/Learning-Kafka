package com.appsdeveloperblog.orders.config;

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

    @Autowired
    private Environment environment;

    private final static Integer TOPIC_REPLICATION_FACTOR=3;
    private final static Integer TOPIC_PARTITIONS=3;

    @Bean
    NewTopic newCreateOrdersEventsTopic(){
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("orders.events.topic.name")))
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic newProductsCommandsTopic(){
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("products.commands.topic.name")))
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic newPaymentsCommandsTopic(){
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("payments.commands.topic.name")))
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic newOrdersCommandsTopic(){
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("orders.commands.topic.name")))
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
