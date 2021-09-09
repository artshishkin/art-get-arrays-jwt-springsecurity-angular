package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.public-urls=/**"
})
class UserResourceUnSecureTest extends BaseUserTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Nested
    class AddNewUserTests {

        @Test
        void addNewUser_correct() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            null,
                            User.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay")
                    .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                    .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", true)
                    .hasFieldOrPropertyWithValue("isNotLocked", true)
                    .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN");
        }

        @Test
        void addNewUser_missedFirstName() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            var responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "ERROR(S) IN PARAMETERS: [FIRSTNAME:SHOULD NOT BE EMPTY]");
        }

        @Test
        void addNewUser_wrongRole() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", "ROLE_FAKE",
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            var responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "ERROR(S) IN PARAMETERS: [ROLE:FAILED TO CONVERT PROPERTY VALUE OF TYPE 'JAVA.LANG.STRING' TO REQUIRED TYPE 'NET.SHYSHKIN.STUDY.FULLSTACK.SUPPORTPORTAL.BACKEND.DOMAIN.ROLE' FOR PROPERTY 'ROLE'; NESTED EXCEPTION IS ORG.SPRINGFRAMEWORK.CORE.CONVERT.CONVERSIONFAILEDEXCEPTION: FAILED TO CONVERT FROM TYPE [JAVA.LANG.STRING] TO TYPE [@JAVAX.VALIDATION.CONSTRAINTS.NOTNULL NET.SHYSHKIN.STUDY.FULLSTACK.SUPPORTPORTAL.BACKEND.DOMAIN.ROLE] FOR VALUE 'ROLE_FAKE'; NESTED EXCEPTION IS JAVA.LANG.ILLEGALARGUMENTEXCEPTION: NO ENUM CONSTANT NET.SHYSHKIN.STUDY.FULLSTACK.SUPPORTPORTAL.BACKEND.DOMAIN.ROLE.ROLE_FAKE]");
        }

        @Test
        void addNewUser_incorrectEmail() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", "not_an_email",
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            var responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "ERROR(S) IN PARAMETERS: [EMAIL:MUST MATCH EMAIL FORMAT]");
        }

        @Test
        void addNewUser_incorrectBoolean() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", "yes",
                    "isNonLocked", "not_a_boolean"
            );

            //when
            var responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "ERROR(S) IN PARAMETERS: [NONLOCKED:FAILED TO CONVERT PROPERTY VALUE OF TYPE 'JAVA.LANG.STRING' TO REQUIRED TYPE 'BOOLEAN' FOR PROPERTY 'NONLOCKED'; NESTED EXCEPTION IS JAVA.LANG.ILLEGALARGUMENTEXCEPTION: INVALID BOOLEAN VALUE [NOT_A_BOOLEAN]]");
        }
    }

    @Nested
    class UpdateUserTests {

        @BeforeEach
        void setUp() {
            user = userRepository
                    .findAll()
                    .stream()
                    .findAny()
                    .orElseGet(() -> userRepository.save(createRandomUser()));
        }

        @Test
        void updateUser_correct_LeaveUsername() {

            //given
            UserDto userDto = createRandomUserDto();
            String currentUsername = user.getUsername();
            userDto.setUsername(currentUsername);
            userDto.setRole(Role.ROLE_MANAGER);

            Map<String, ?> paramMap = Map.of(
                    "currentUsername", currentUsername,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .exchange(
                            "/user/{currentUsername}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            HttpMethod.PUT,
                            null,
                            User.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay")
                    .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                    .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", true)
                    .hasFieldOrPropertyWithValue("isNotLocked", true)
                    .hasFieldOrPropertyWithValue("role", "ROLE_MANAGER");
        }

        @Test
        void updateUser_correct_ChangeUsername() {

            //given
            UserDto userDto = createRandomUserDto();
            String currentUsername = user.getUsername();

            userDto.setRole(Role.ROLE_HR);

            Map<String, ?> paramMap = Map.of(
                    "currentUsername", currentUsername,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", userDto.isActive(),
                    "isNonLocked", userDto.isNonLocked()
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .exchange(
                            "/user/{currentUsername}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            HttpMethod.PUT,
                            null,
                            User.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay")
                    .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                    .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", true)
                    .hasFieldOrPropertyWithValue("isNotLocked", true)
                    .hasFieldOrPropertyWithValue("role", "ROLE_HR")
                    .satisfies(u -> assertThat(u.getAuthorities()).hasSize(2));
        }

        @Test
        void updateUser_missedLastName() {

            //given
            UserDto userDto = createRandomUserDto();
            String currentUsername = user.getUsername();

            userDto.setRole(Role.ROLE_USER);

            Map<String, ?> paramMap = Map.of(
                    "currentUsername", currentUsername,
                    "firstName", userDto.getFirstName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", userDto.isActive(),
                    "isNonLocked", userDto.isNonLocked()
            );

            //when
            var responseEntity = restTemplate
                    .exchange(
                            "/user/{currentUsername}?username={username}&email={email}" +
                                    "&firstName={firstName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            HttpMethod.PUT,
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "ERROR(S) IN PARAMETERS: [LASTNAME:SHOULD NOT BE EMPTY]");
        }

        @Test
        void updateUser_absentUser() {

            //given
            UserDto userDto = createRandomUserDto();
            String currentUsername = FAKER.name().username();
            userDto.setUsername(currentUsername);
            userDto.setRole(Role.ROLE_MANAGER);

            Map<String, ?> paramMap = Map.of(
                    "currentUsername", currentUsername,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNonLocked", String.valueOf(userDto.isNonLocked())
            );

            //when
            var responseEntity = restTemplate
                    .exchange(
                            "/user/{currentUsername}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&nonLocked={isNonLocked}",
                            HttpMethod.PUT,
                            null,
                            HttpResponse.class,
                            paramMap
                    );

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", String.format("User with username `%s` not found", currentUsername).toUpperCase());
        }

    }
}