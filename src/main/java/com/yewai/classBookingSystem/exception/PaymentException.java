package com.yewai.classBookingSystem.exception;

import lombok.Getter;

import java.io.Serial;
import java.util.Map;

@Getter
public class PaymentException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;
    private final Map<String, String> details;

    public PaymentException(String message, Map<String, String> details) {
        super(message);
        this.details = details;
    }

    public PaymentException(String message) {
        super(message);
        this.details = null;
    }
}