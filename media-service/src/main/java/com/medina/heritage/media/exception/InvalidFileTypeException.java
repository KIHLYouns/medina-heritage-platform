package com.medina.heritage.media.exception;

/**
 * Exception levée quand le type de fichier n'est pas autorisé.
 */
public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String mimeType) {
        super("File type not allowed: " + mimeType);
    }

    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
