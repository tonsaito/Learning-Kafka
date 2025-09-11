package com.appsdeveloperblog.orders.service.handler;

import com.appsdeveloperblog.core.dto.commands.ApprovedOrderCommand;
import com.appsdeveloperblog.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics="${orders.commands.topic.name}")
public class OrderCommandsHandler {

    @Autowired
    private Environment environment;

    @Autowired
    private OrderService orderService;

    @KafkaHandler
    public void handleCommand(@Payload ApprovedOrderCommand command){
        orderService.approveOrder(command.getOrderId());
    }
}
