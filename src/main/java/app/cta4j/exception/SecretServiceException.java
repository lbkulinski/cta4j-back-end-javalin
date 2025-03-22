package app.cta4j.exception;

public final class SecretServiceException extends RuntimeException {
    public SecretServiceException(String message) {
        super(message);
    }

    public SecretServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
