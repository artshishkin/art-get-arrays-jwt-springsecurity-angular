package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
}
