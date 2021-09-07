package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String message) {
        super(message);
    }
}
