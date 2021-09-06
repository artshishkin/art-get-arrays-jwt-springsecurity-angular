package net.shyshkin.study.fullstack.supportportal.backend.exception.domain;

public class EmailExistsException extends Exception{
    public EmailExistsException(String message) {
        super(message);
    }
}
