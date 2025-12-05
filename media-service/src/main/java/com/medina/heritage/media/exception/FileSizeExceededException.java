package com.medina.heritage.media.exception;

/**
 * Exception levée quand la taille du fichier dépasse la limite.
 */
public class FileSizeExceededException extends RuntimeException {

    public FileSizeExceededException(long size, long maxSize) {
        super(String.format("File size %d bytes exceeds maximum allowed size %d bytes", size, maxSize));
    }
}
