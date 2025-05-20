package com.ProjectGraduation.comment.exception;

public class UnauthorizedCommentDeletionException extends RuntimeException{
    public UnauthorizedCommentDeletionException(String message) {
        super(message);
    }

    public UnauthorizedCommentDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
