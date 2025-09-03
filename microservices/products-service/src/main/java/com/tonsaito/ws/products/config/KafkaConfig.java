package com.tonsaito.ws.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.name}")
    private String topicName;

    @Value("${app.kafka.topic.partitions}")
    private Integer topicPartitions;

    @Value("${app.kafka.topic.replicas}")
    private Integer topicReplicas;

    @Value("${app.kafka.topic.sync.replicas}")
    private String topicSyncReplicas;

    @Bean
    NewTopic createTopic(){
        return TopicBuilder.name(topicName)
                .partitions(topicPartitions)
                .replicas(topicReplicas)
                .configs(Map.of("min.insync.replicas", topicSyncReplicas))
                .build();
    }
}
