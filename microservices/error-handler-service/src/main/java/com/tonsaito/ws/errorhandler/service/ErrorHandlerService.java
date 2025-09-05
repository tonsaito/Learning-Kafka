package com.tonsaito.ws.errorhandler.service;

import com.tonsaito.lib.core.model.ErrorHandlerModel;
import org.springframework.stereotype.Component;

public interface ErrorHandlerService {
    void sendErrorMessage(ErrorHandlerModel errorHandlerModel) throws Exception;
}
