package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User with username `" + username + "` not found");
    }

    @Test
    void addNewUser_correct() {

        //given
        UserDto randomUserDto = createRandomUserDto();

        //when
        User newUser = userService.addNewUser(randomUserDto);

        //then
        log.debug("Added new user: {}", newUser);
        assertThat(newUser)
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay")
                .hasFieldOrPropertyWithValue("username", randomUserDto.getUsername())
                .hasFieldOrPropertyWithValue("email", randomUserDto.getEmail())
                .hasFieldOrPropertyWithValue("firstName", randomUserDto.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", randomUserDto.getLastName())
                .hasFieldOrPropertyWithValue("isActive", randomUserDto.isActive())
                .hasFieldOrPropertyWithValue("isNotLocked", randomUserDto.isNotLocked())
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN")
        ;
    }

    @Test
    void addNewUser_incorrectRole() {

        //given
        UserDto randomUserDto = createRandomUserDto();

        //when
        ThrowableAssert.ThrowingCallable execution = () -> {
            randomUserDto.setRole(Role.valueOf("FAKE_ROLE"));
            User user = userService.addNewUser(randomUserDto);
        };

        //then
        assertThatThrownBy(execution)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No enum constant net.shyshkin.study.fullstack.supportportal.backend.domain.Role.FAKE_ROLE");
    }

    @Test
    void updateProfileImage() throws IOException {
        //given
        User fakeUser = createRandomUser();
        user = userRepository.save(fakeUser);
        String username = user.getUsername();

        //when
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg",
                "image/jpeg", ("Spring Framework" + UUID.randomUUID()).getBytes());
        userService.updateProfileImage(username, multipartFile);

        //then
        Path path = Path.of(FileConstant.USER_FOLDER, user.getUserId().toString(), FileConstant.USER_IMAGE_FILENAME);
        log.debug("Path of created file: {}", path);
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.getLastModifiedTime(path).toInstant()).isCloseTo(Instant.now(), within(100, ChronoUnit.MILLIS));
    }
}