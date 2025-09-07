package com.tonsaito.ws.emailnotification.handler;

import com.tonsaito.lib.core.model.ProductCreatedEventModel;
import com.tonsaito.ws.emailnotification.entity.ProcessEventEntity;
import com.tonsaito.ws.emailnotification.entity.repository.ProcessedEventRepository;
import com.tonsaito.ws.emailnotification.exception.NotRetryableException;
import com.tonsaito.ws.emailnotification.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(topics="${app.kafka.topic.name}")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaHandler
    public void handle(@Payload ProductCreatedEventModel productCreatedEvent, @Header("messageId") String messageId, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){
        String requestUrl = "http://localhost:8082/response/200";

        ProcessEventEntity processEventEntity = processedEventRepository.findByMessageId(messageId);

        if(processEventEntity != null){
            LOGGER.error("Found duplicate message id: {}",processEventEntity.getMessageId());
            return;
        }

        LOGGER.info("Received a new event: "+productCreatedEvent.getTitle());

        try{
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);
            if(response.getStatusCode().value() == HttpStatus.OK.value()){
                System.out.println("Receive new Message ("+messageKey+") =====================================");
                System.out.println("Message ID: " + messageId);
                System.out.println("Title: " + productCreatedEvent.getTitle());
                System.out.println("Price: " + productCreatedEvent.getPrice());
                System.out.println("Quantity: " + productCreatedEvent.getQuantity());
            }
        } catch (ResourceAccessException ex){
            LOGGER.error(ex.getMessage());
            throw  new RetryableException(ex);
        } catch (HttpServerErrorException ex){
            LOGGER.error("HttpServerErrorException: {}",ex.getMessage());
            throw  new NotRetryableException(ex);
        } catch (Exception ex){
            LOGGER.error("General Exception: {}",ex.getMessage());
            throw  new NotRetryableException(ex);
        }

        //save unique Message ID once. If try to save the second time, will throw exception
        try{
            processedEventRepository.save(new ProcessEventEntity(messageId, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException ex){
            LOGGER.error("Already Consumed the message | DataIntegrityViolationException: {}",ex.getMessage());
            throw  new NotRetryableException(ex);
        }

    }
}
