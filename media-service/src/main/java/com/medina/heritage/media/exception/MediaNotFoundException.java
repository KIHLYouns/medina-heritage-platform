package com.medina.heritage.media.exception;

import java.util.UUID;

/**
 * Exception levée quand un média n'est pas trouvé.
 */
public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(UUID id) {
        super("Media not found with id: " + id);
    }

    public MediaNotFoundException(String message) {
        super(message);
    }
}
