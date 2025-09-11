package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.dto.commands.*;
import com.appsdeveloperblog.core.dto.events.*;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@KafkaListener(topics={"${orders.events.topic.name}","${products.events.topic.name}", "${payments.events.topic.name}"})
public class OrderSaga {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    Environment environment;

    @Autowired
    OrderHistoryService orderHistoryService;

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event){
        ReserveProductCommand command = new ReserveProductCommand(event.getProductId(), event.getProductQuantity(), event.getOrderId());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("products.commands.topic.name")), command);

        orderHistoryService.add(event.getOrderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event){
        ProcessPaymentCommand command = new ProcessPaymentCommand(event.getOrderId(), event.getProductId(), event.getProductPrice(), event.getProductQuantity());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("payments.commands.topic.name")), command);
    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentProcessedEvent event){
        ApprovedOrderCommand command = new ApprovedOrderCommand(event.getOrderId());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("orders.commands.topic.name")), command);
    }

    @KafkaHandler
    public void handlerEvent(@Payload OrderApprovedEvent event){
        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);
    }

    @KafkaHandler
    public void handlerEvent(@Payload PaymentFailedEvent event){
        CancelProductReservationCommand command = new CancelProductReservationCommand(event.getProductId(), event.getOrderId(), event.getProductQuantity());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("products.commands.topic.name")), command);
    }

    @KafkaHandler
    public void handlerEvent(@Payload ProductReservationCancelEvent event){
        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId());

        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("orders.commands.topic.name")), command);


        orderHistoryService.add(event.getOrderId(), OrderStatus.REJECTED);
    }
}
