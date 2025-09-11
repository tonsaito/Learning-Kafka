package com.appsdeveloperblog.orders.service;

import com.appsdeveloperblog.core.dto.Order;
import com.appsdeveloperblog.core.dto.events.OrderApprovedEvent;
import com.appsdeveloperblog.core.dto.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.dao.jpa.entity.OrderEntity;
import com.appsdeveloperblog.orders.dao.jpa.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Environment environment;


    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        OrderCreatedEvent event = new OrderCreatedEvent(entity.getId(), entity.getCustomerId(), entity.getProductId(), entity.getProductQuantity());



        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("orders.events.topic.name")), event);

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    public void approveOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(entity, "No order is found with id "+orderId + " in database table");
        entity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(entity);

        OrderApprovedEvent event = new OrderApprovedEvent(orderId);
        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("orders.events.topic.name")), event);
    }

    @Override
    public void rejectOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(entity, "No order is found with id "+orderId + " in database table");
        entity.setStatus(OrderStatus.REJECTED);
        orderRepository.save(entity);
    }

}
