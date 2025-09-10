package com.tonsaito.ws.emailnotification;

import com.tonsaito.lib.core.model.ProductCreatedEventModel;
import com.tonsaito.ws.emailnotification.entity.ProcessEventEntity;
import com.tonsaito.ws.emailnotification.entity.repository.ProcessedEventRepository;
import com.tonsaito.ws.emailnotification.handler.ProductCreatedEventHandler;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductCreatedEventHandlerIntegrationTest {

    @MockitoBean
    ProcessedEventRepository processedEventRepository;

    @MockitoBean
    RestTemplate restTemplate;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    ProductCreatedEventHandler productCreatedEventHandler;


    @Test
    public void testProductCreatedEventHandler_OnProductCreated_HandleEvent() throws ExecutionException, InterruptedException {
        //Arrange
        ProductCreatedEventModel productCreatedEventModel = new ProductCreatedEventModel();
        productCreatedEventModel.setPrice(new BigDecimal(100));
        productCreatedEventModel.setProductId(UUID.randomUUID().toString());
        productCreatedEventModel.setQuantity(1);
        productCreatedEventModel.setTitle("Iphone X");

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreatedEventModel.getProductId();

        ProducerRecord<String, Object> record = new ProducerRecord<>("product-created-events-topic", messageKey, productCreatedEventModel);

        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        ProcessEventEntity processEventEntity = new ProcessEventEntity();
        when(processedEventRepository.findByMessageId(anyString())).thenReturn(processEventEntity);
        when(processedEventRepository.save(any(ProcessEventEntity.class))).thenReturn(null);

        String responseBody = "{\"key\":\"value\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), isNull(), eq(String.class))).thenReturn(responseEntity);
        //Act
        kafkaTemplate.send(record).get();

        //Assert
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreatedEventModel> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEventModel.class);

        verify(productCreatedEventHandler, timeout(5000).times(1)).handle(eventCaptor.capture(), messageIdCaptor.capture(), messageKeyCaptor.capture());

        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());
        assertEquals(productCreatedEventModel.getProductId(), eventCaptor.getValue().getProductId());

    }
}
