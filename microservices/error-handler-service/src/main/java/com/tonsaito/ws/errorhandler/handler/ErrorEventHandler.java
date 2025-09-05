package com.tonsaito.ws.errorhandler.handler;

import com.tonsaito.lib.core.model.ErrorHandlerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics="${app.kafka.topic.name}")
public class ErrorEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(ErrorHandlerModel errorHandlerModel){
        StringBuilder sb = new StringBuilder();
        sb.append("\n>>>> New error received ============ ").append(errorHandlerModel.getMessage());
        sb.append("\n>>>> ON  ").append(errorHandlerModel.getTimestamp());
        sb.append("\n>>>>  ").append(errorHandlerModel.getMessage());
        sb.append("\n>>>> Stacktrace: ");
        sb.append("\n>>>>  ").append(errorHandlerModel.getStacktrace());
        LOGGER.info(sb.toString());

    }
    
}
