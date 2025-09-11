package com.appsdeveloperblog.products.handler;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@KafkaListener(topics = "${products.commands.topic.name}")
public class ProductsCommandsHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProductService productService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Environment environment;

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command){

        try {
            Product desiredProduct = new Product(command.getProductId(), command.getProductQuantity());
            productService.reserve(desiredProduct, command.getOrderId());
            Product reservedProduct = productService.reserve(desiredProduct, command.getOrderId());

            ProductReservedEvent productReservedEvent = new ProductReservedEvent(command.getOrderId(), command.getProductId(), reservedProduct.getPrice(), command.getProductQuantity());

            kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("products.events.topic.name")), productReservedEvent);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());

            ProductReservationFailedEvent event = new ProductReservationFailedEvent(command.getProductId(), command.getProductId(), command.getProductQuantity());

            kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("products.events.topic.name")), event);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelProductReservationCommand command){
        Product productToCancel = new Product(command.getProductId(), command.getProductQuantity());
        productService.cancelReservation(productToCancel, command.getOrderId());

        ProductReservationCancelEvent event = new ProductReservationCancelEvent(command.getProductId(), command.getOrderId());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("products.events.topic.name")), event);
    }
}
