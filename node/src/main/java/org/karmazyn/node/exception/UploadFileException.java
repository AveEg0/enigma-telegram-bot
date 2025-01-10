package org.karmazyn.node.exception;

public class UploadFileException extends RuntimeException {
    public UploadFileException(String message) {
        super(message);
    }
    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }
    public UploadFileException(Throwable cause) {
        super(cause);
    }
}
