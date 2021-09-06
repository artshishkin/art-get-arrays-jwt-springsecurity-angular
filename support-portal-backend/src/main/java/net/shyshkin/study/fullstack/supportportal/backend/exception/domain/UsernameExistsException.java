package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class UsernameExistsException extends Exception{
    public UsernameExistsException(String message) {
        super(message);
    }
}
