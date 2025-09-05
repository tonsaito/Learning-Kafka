package com.tonsaito.lib.core.model;

import java.util.Date;

public class ErrorHandlerModel {

    private Date timestamp;
    private String message;
    private String stacktrace;

    public ErrorHandlerModel() {
    }

    public ErrorHandlerModel(Date timestamp, String message, String stacktrace) {
        this.timestamp = timestamp;
        this.message = message;
        this.stacktrace = stacktrace;
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

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }
}
