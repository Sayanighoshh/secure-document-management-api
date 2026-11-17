package com.sayani.secure_auth_api.exception;

import lombok.Getter;

@Getter
public class CustomMessageException extends RuntimeException {

    private final String message;
    private final String status;

    public CustomMessageException(String message) {
        super(message);
        this.message = message;
        this.status = "FAIL";
    }

    public CustomMessageException(String message, String status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}
