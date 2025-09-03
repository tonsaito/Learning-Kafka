package com.tonsaito.ws.products.model;

import java.util.Date;

public class ResponseModel {

    private Date timestamp;
    private String message;
    private String details;

    public ResponseModel() {
    }

    public ResponseModel(String message, String details) {
        this.timestamp = new Date();
        this.message = message;
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
