package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UserNotFoundException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class UserServiceTest extends BaseUserTest {

    @Autowired
    UserService userService;

    @Test
    void loadUserByUsername_present() throws InterruptedException {

        //given
        User fakeUser = createRandomUser();
        user = userRepository.save(fakeUser);
        String username = user.getUsername();
        Thread.sleep(500);

        //when
        UserDetails userDetails = userService.loadUserByUsername(username);

        //then
        log.debug("User Details: {}", userDetails);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);

        Optional<User> byUsername = userRepository.findByUsername(username);
        assertThat(byUsername)
                .hasValueSatisfying(
                        u -> assertThat(u.getLastLoginDate().minus(500L, ChronoUnit.MILLIS))
                                .isCloseTo(u.getLastLoginDateDisplay(), within(150, ChronoUnit.MILLIS)));
    }

    @Test
    void loadUserByUsername_absent() {

        //given
        String username = UUID.randomUUID().toString();

        //when
        ThrowableAssert.ThrowingCallable execution = () -> {
            UserDetails userDetails = userService.loadUserByUsername(username);
        };

        //then
        assertThatThrownBy(execution)
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with username `" + username + "` not found");
    }
}