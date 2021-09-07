package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
