package com.medina.heritage.gamification.exception;

/**
 * Exception levée quand le solde est insuffisant pour une opération.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(int requested, int available) {
        super(String.format("Insufficient balance: requested %d points but only %d available", requested, available));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
