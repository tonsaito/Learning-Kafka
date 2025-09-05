package com.tonsaito.ws.emailnotification.config;

import com.tonsaito.lib.core.model.ErrorHandlerModel;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Autowired
    Environment environment;

    @Bean
    ConsumerFactory<String, Object> consumerFactory(){
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.consumer.bootstrap-servers"));
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configMap.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate){
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate));

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return  factory;
    }

//    @Bean
//    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory){
//        return new KafkaTemplate<>(producerFactory);
//    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(pf);

        template.setProducerListener(new ProducerListener<>() {

            @Override
            public void onSuccess(ProducerRecord<String, Object> record, RecordMetadata metadata) {
                if(metadata.topic().contains("-dlt")){
                    StringBuilder stacktrace = new StringBuilder();
                    stacktrace.append("Service: ").append(environment.getProperty("spring.application.name"));
                    stacktrace.append("\nTopic: ").append(metadata.topic());
                    stacktrace.append("\nPartition:").append(record.partition());
                    stacktrace.append("\nKey: ").append(record.key());
                    stacktrace.append("\nValue:\n").append(new String((byte[]) record.value(), StandardCharsets.UTF_8));
                    ErrorHandlerModel payload = new ErrorHandlerModel(
                            new Date(),
                            "Error during message handler. See stacktrace for more details",
                            stacktrace.toString()
                    );

                    template.send("error-handler-events-topic", "error", payload);
                    System.out.println(stacktrace.toString());
                }
            }

            @Override
            public void onError(ProducerRecord<String, Object> record, RecordMetadata metadata, Exception exception) {
                // Intercept failed messages (including DLT)
                System.out.println("Intercepted DLT message: " + record.value());
            }
        });

        return template;
    }

    @Bean
    ProducerFactory<String, Object> producerFactory(){
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.consumer.bootstrap-servers"));
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configMap);
    }
}