package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String message) {
        super(message);
    }
}
