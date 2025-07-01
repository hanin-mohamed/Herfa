package com.ProjectGraduation.auth.exception;

public class InvalidFCMTokenException extends RuntimeException {
    public InvalidFCMTokenException(String message) {
        super(message);
    }
}