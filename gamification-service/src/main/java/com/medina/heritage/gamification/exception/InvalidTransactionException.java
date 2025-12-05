package com.medina.heritage.gamification.exception;

/**
 * Exception levée quand une transaction est invalide.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
