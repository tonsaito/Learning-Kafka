package com.tonsaito.ws.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.tonsaito.lib.core.model.ProductCreatedEventModel;
import com.tonsaito.ws.products.model.ProductModel;
import com.tonsaito.ws.products.service.ProductService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") // application-test.properties
@EmbeddedKafka(partitions=3, count=3, controlledShutdown=true)
@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    Environment environment;

    private KafkaMessageListenerContainer<String, ProductCreatedEventModel> container;
    private BlockingQueue<ConsumerRecord<String, ProductCreatedEventModel>> records;

    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());

        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("app.kafka.topic.name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, ProductCreatedEventModel>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

    }

    @Test
    void testCreateProduct_whenGivenValidProductDetails_successfullySendsKafkaMessage() throws Exception {

        // Arrange

        String title="iPhone 11";
        BigDecimal price = new BigDecimal(600);
        Integer quantity = 1;

        ProductModel createProductRestModel = new ProductModel();
        createProductRestModel.setPrice(price);
        createProductRestModel.setQuantity(quantity);
        createProductRestModel.setTitle(title);

        // Act

        productService.createProductSync(createProductRestModel);


        // Assert
        ConsumerRecord<String, ProductCreatedEventModel> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertNotNull(message.key());
        System.out.println(message.value());
        ProductCreatedEventModel productCreatedEvent = message.value();
        assertEquals(createProductRestModel.getQuantity(), productCreatedEvent.getQuantity());
        assertEquals(createProductRestModel.getTitle(), productCreatedEvent.getTitle());
        assertEquals(createProductRestModel.getPrice(), productCreatedEvent.getPrice());
    }


    private Map<String, Object> getConsumerProperties() {
        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.group-id")),
                JsonDeserializer.TRUSTED_PACKAGES, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages")),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.auto-offset-reset"))
        );
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }


}