package com.appsdeveloperblog.payments.service.handler;

import com.appsdeveloperblog.core.dto.Payment;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.exceptions.CreditCardProcessorUnavailableException;
import com.appsdeveloperblog.payments.service.PaymentService;
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
@KafkaListener(topics="${payments.commands.topic.name}")
public class PaymentsCommandsHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsCommandsHandler.class);
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Environment environment;

    @KafkaHandler
    private void handle(@Payload ProcessPaymentCommand command){
        try {
            Payment payment = new Payment(command.getOrderId(), command.getProductId(), command.getProductPrice(), command.getProductQuantity());
            Payment processedPayment = paymentService.process(payment);

            PaymentProcessedEvent event = new PaymentProcessedEvent(command.getOrderId(), payment.getId());
            kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("payments.events.topic.name")), event);
        } catch (CreditCardProcessorUnavailableException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(command.getOrderId(), command.getProductId(), command.getProductQuantity());
            kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("payments.events.topic.name")), failedEvent);
        }
    }
}
