package com.tonsaito.ws.errorhandler.service.impl;

import com.tonsaito.lib.core.model.ErrorHandlerModel;
import com.tonsaito.ws.errorhandler.service.ErrorHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ErrorHandlerServiceImpl implements ErrorHandlerService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${app.kafka.topic.name}")
    private String topicName;

    @Autowired
    KafkaTemplate<String, ErrorHandlerModel> kafkaTemplate;

    @Override
    public void sendErrorMessage(ErrorHandlerModel errorHandlerModel) throws Exception {
        errorHandlerModel.setTimestamp(new Date());
        SendResult<String, ErrorHandlerModel> result = kafkaTemplate.send(topicName, "error", errorHandlerModel).get();
    }
}
