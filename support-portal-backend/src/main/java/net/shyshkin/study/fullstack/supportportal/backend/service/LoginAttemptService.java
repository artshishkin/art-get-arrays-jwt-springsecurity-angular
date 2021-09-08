package net.shyshkin.study.fullstack.supportportal.backend.service;

public interface LoginAttemptService {

    static final int MAX_ATTEMPTS = 5;
    static final int ATTEMPT_INCREMENT = 1;

    void loginFailed(String username);

    void loginSucceeded(String username);

    boolean hasExceededMaxAttempts(String username);

}
