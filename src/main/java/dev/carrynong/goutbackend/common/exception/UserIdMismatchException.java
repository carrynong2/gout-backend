package dev.carrynong.goutbackend.common.exception;

public class UserIdMismatchException extends RuntimeException {
    public UserIdMismatchException() {
        super();
    }

    public UserIdMismatchException(String message) {
        super(message);
    }
}