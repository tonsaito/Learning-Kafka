package com.tonsaito.ws.products.service.impl;

import com.tonsaito.ws.products.model.ProductModel;
import com.tonsaito.ws.products.service.ProductCreatedEvent;
import com.tonsaito.ws.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${app.kafka.topic.name}")
    private String topicName;

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProductAsync(ProductModel productModel) {
        String productId = UUID.randomUUID().toString();
        //TODO: persist Product Details in db, before publishing event

        //Send Event
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, productModel.getTitle(), productModel.getPrice(), productModel.getQuantity());
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate.send(topicName, productId, productCreatedEvent);

        future.whenComplete(((sendResult, throwable) -> {
            if(throwable != null){
                LOGGER.error("Failed to send message: "+throwable.getMessage());
            } else{
                LOGGER.info("Message sent successfully "+sendResult.getRecordMetadata());
            }
        }));

        LOGGER.info("****** Returning product ID. ASync check message.");
        return productId;
    }

    @Override
    public String createProductSync(ProductModel productModel) throws Exception {
        String productId = UUID.randomUUID().toString();
        //TODO: persist Product Details in db, before publishing event

        //Send Event
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, productModel.getTitle(), productModel.getPrice(), productModel.getQuantity());

        LOGGER.info("****** Before publishing a ProductCreatedEvent");

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(topicName, productId, productCreatedEvent).get();

        LOGGER.info("****** Partition: "+result.getRecordMetadata().partition());
        LOGGER.info("****** Topic: "+result.getRecordMetadata().topic());
        LOGGER.info("****** Offset: "+result.getRecordMetadata().offset());
        LOGGER.info("****** Returning product ID. Sync check message.");
        return productId;
    }
}
