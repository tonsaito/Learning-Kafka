package com.tonsaito.ws.emailnotification.config;

import com.google.gson.Gson;
import com.tonsaito.lib.core.model.ErrorHandlerModel;
import com.tonsaito.ws.emailnotification.exception.NotRetryableException;
import com.tonsaito.ws.emailnotification.exception.RetryableException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class KafkaConfig {

    @Autowired
    Environment environment;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(5000 , 3));
        defaultErrorHandler.addNotRetryableExceptions(NotRetryableException.class);
        defaultErrorHandler.addNotRetryableExceptions(RetryableException.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return  factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(pf);

        template.setProducerListener(new ProducerListener<>() {

            @Override
            public void onSuccess(ProducerRecord<String, Object> record, RecordMetadata metadata) {
                if(metadata.topic().contains("-dlt")){
                    StringBuilder stacktrace = new StringBuilder();
                    ErrorHandlerModel payload = new ErrorHandlerModel(
                            new Date(),
                            "Error during message handler. See stacktrace for more details",
                            stacktrace.toString()
                    );
                    stacktrace.append("Service: ").append(environment.getProperty("spring.application.name"));
                    stacktrace.append("\nTopic: ").append(metadata.topic());
                    stacktrace.append("\nPartition:").append(record.partition());
                    stacktrace.append("\nKey: ").append(record.key());
                    try{
                        String tempStr = new String((byte[]) record.value(), StandardCharsets.UTF_8);
                        stacktrace.append("\nValue:\n").append(tempStr);
                    } catch (Exception ex){
                        stacktrace.append("\nValue:\n").append(new Gson().toJson(record.value()));
                        LOGGER.error(ex.getMessage());
                    } finally {
                        payload.setStacktrace(stacktrace.toString());
                    }
                    try{
                        template.send(Objects.requireNonNull(environment.getProperty("app.kafka.topic.error.name")), "error", payload);
                    } catch (Exception ex){
                        LOGGER.error(ex.getMessage());
                    }
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