package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class NotAnImageFileException extends RuntimeException {
    public NotAnImageFileException(String message) {
        super(message);
    }
}
