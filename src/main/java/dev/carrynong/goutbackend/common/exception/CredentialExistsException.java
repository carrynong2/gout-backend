package dev.carrynong.goutbackend.common.exception;

public class CredentialExistsException extends RuntimeException {
    public CredentialExistsException() {
        super();
    }

    public CredentialExistsException(String message) {
        super(message);
    }
}
