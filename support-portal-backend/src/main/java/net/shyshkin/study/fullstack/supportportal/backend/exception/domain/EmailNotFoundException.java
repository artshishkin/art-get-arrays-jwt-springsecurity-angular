package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String message) {
        super(message);
    }
}
