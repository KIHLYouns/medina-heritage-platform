package com.medina.heritage.gamification.exception;

import java.util.UUID;

/**
 * Exception levée quand un wallet n'est pas trouvé.
 */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID userId) {
        super("Wallet not found for user: " + userId);
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}
