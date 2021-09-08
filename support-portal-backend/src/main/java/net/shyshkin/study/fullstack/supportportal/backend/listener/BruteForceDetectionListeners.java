package net.shyshkin.study.fullstack.supportportal.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BruteForceDetectionListeners  {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getPrincipal().toString();
        log.debug("{} failed to login", username);
        loginAttemptService.loginFailed(username);
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            log.debug("{} successfully logged in", username);
            loginAttemptService.loginSucceeded(username);
        }
    }
}
