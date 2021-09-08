package net.shyshkin.study.fullstack.supportportal.backend.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Primary
public class GuavaCacheLoginAttemptService implements LoginAttemptService {

    private LoadingCache<String, Integer> loginAttemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<>() {
                @Override
                public Integer load(String key) throws Exception {
                    return 0;
                }
            });

    @Override
    public void loginFailed(String username) {
        int attempts = getAttempts(username);
        loginAttemptsCache.put(username, attempts + ATTEMPT_INCREMENT);
    }

    @Override
    public void loginSucceeded(String username) {
        evictUserFromCache(username);
    }

    @Override
    public boolean hasExceededMaxAttempts(String username) {
        return getAttempts(username) >= MAX_ATTEMPTS;
    }

    @Override
    public void evictUserFromCache(String username) {
        loginAttemptsCache.invalidate(username);
    }

    private int getAttempts(String username) {
        Integer attempts = loginAttemptsCache.getIfPresent(username);
        return Objects.requireNonNullElse(attempts, 0);
    }
}
