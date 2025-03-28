package com.ProjectGraduation.product.exception;

public class UnauthorizedMerchantException extends RuntimeException {
    public UnauthorizedMerchantException(String message) {
        super(message);
    }
}