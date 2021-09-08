package net.shyshkin.study.fullstack.supportportal.backend.service;

public interface LoginAttemptService {

    int MAX_ATTEMPTS = 5;
    int ATTEMPT_INCREMENT = 1;

    void loginFailed(String username);

    void loginSucceeded(String username);

    boolean hasExceededMaxAttempts(String username);

    void evictUserFromCache(String username);
}
