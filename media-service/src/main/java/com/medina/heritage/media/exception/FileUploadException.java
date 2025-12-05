package com.medina.heritage.media.exception;

/**
 * Exception levée quand l'upload du fichier échoue.
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
