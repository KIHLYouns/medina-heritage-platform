package com.medina.heritage.gamification.exception;

import java.util.UUID;

/**
 * Exception levée quand une transaction dupliquée est détectée.
 */
public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(UUID referenceId, String referenceType, String reasonCode) {
        super(String.format("Duplicate transaction detected: referenceId=%s, referenceType=%s, reasonCode=%s", 
                referenceId, referenceType, reasonCode));
    }

    public DuplicateTransactionException(String message) {
        super(message);
    }
}
