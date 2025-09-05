package com.tonsaito.ws.errorhandler.controller;

import com.tonsaito.lib.core.model.ErrorHandlerModel;
import com.tonsaito.lib.core.model.ResponseModel;
import com.tonsaito.ws.errorhandler.service.ErrorHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/error-message")
public class ErrorHandlerController {

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @PostMapping
    public ResponseEntity<ResponseModel> generateErrorMessage(@RequestBody ErrorHandlerModel errorHandlerModel){
        ResponseModel responseModel = new ResponseModel();
        int httpStatus;
        try {
            errorHandlerService.sendErrorMessage(errorHandlerModel);
            httpStatus = HttpStatus.CREATED.value();
            responseModel.setHttpStatus(httpStatus);
            responseModel.setMessage("Successfully sent the message.");
        } catch (Exception e){
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            responseModel.setHttpStatus(httpStatus);
            responseModel.setMessage("Error");
            responseModel.setDetails(e.getMessage());
        }
        return ResponseEntity.status(httpStatus).body(responseModel);
    }
}
