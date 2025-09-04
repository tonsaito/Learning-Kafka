package com.tonsaito.ws.emailnotification.handler;

import com.tonsaito.lib.core.model.ProductCreatedEventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics="product-created-events-topic")
public class ProductCreatedEvenetHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(ProductCreatedEventModel productCreatedEvent){
        LOGGER.info("Received a new event: "+productCreatedEvent.getTitle());

    }
}
