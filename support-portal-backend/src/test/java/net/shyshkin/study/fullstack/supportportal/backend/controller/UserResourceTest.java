package net.shyshkin.study.fullstack.supportportal.backend.controller;

import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.service.LoginAttemptService;
import net.shyshkin.study.fullstack.supportportal.backend.utility.JwtTokenProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants.JWT_TOKEN_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class UserResourceTest extends BaseUserTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    JWTVerifier jwtVerifier;

    @Test
    @Order(10)
    void showUserHome_forbidden() {

        //when
        var responseEntity = restTemplate.getForEntity("/user/home", HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(403),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(FORBIDDEN),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("FORBIDDEN"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo("You need to log in to access this page")
                ));
    }

    @Test
    @Order(20)
    void showUserHome_correctToken() {

        //given
        User fakeUser = createRandomUser();
        user = userRepository.save(fakeUser);
        String validToken = jwtTokenProvider.generateJwtToken(new UserPrincipal(user));
        log.debug("JWT Token: `{}`", validToken);

        //when
        RequestEntity<?> requestEntity = RequestEntity
                .get("/user/home")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        var responseEntity = restTemplate.exchange(requestEntity, String.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .isEqualTo("Application works");
    }

    @Test
    @Order(30)
    void registerUser_new() {

        //given
        User fakeUser = createRandomUser();

        //when
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/user/register", fakeUser, User.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        User registeredUser = responseEntity.getBody();
        assertThat(registeredUser)
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay")
                .hasFieldOrPropertyWithValue("username", fakeUser.getUsername())
                .hasFieldOrPropertyWithValue("email", fakeUser.getEmail())
                .hasFieldOrPropertyWithValue("firstName", fakeUser.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", fakeUser.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_USER")
        ;
        user = registeredUser;
    }

    @Test
    @Order(40)
    void registerUser_usernameExists() {

        //given
        User fakeUser = createRandomUser();
        String username = user.getUsername();
        fakeUser.setUsername(username);
        String expectedMessage = ("Username `" + username + "` is already taken. Please select another one").toUpperCase();

        //when
        ResponseEntity<HttpResponse> responseEntity = restTemplate.postForEntity("/user/register", fakeUser, HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(400),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(BAD_REQUEST),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("BAD REQUEST"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage)
                ));
    }

    @Test
    @Order(41)
    void registerUser_emailExists() {

        //given
        User fakeUser = createRandomUser();
        String email = user.getEmail();
        fakeUser.setEmail(email);
        String expectedMessage = ("User with email `" + email + "` is already registered").toUpperCase();

        //when
        ResponseEntity<HttpResponse> responseEntity = restTemplate.postForEntity("/user/register", fakeUser, HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(400),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(BAD_REQUEST),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("BAD REQUEST"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage)
                ));
    }

    @Test
    @Order(50)
    void loginUser_existingUser() {

        //given
        User fakeUser = createRandomUser();
        String password = fakeUser.getPassword().replace("{noop}", "");
        String username = fakeUser.getUsername();
        userRepository.save(fakeUser);
        String expectedMessage = "User logged in successfully";

        //when
        User userLogin = User.builder()
                .username(username)
                .password(password)
                .build();
        var responseEntity = restTemplate.postForEntity("/user/login", userLogin, HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(200),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(OK),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("OK"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage)
                ));
        String token = responseEntity.getHeaders().getFirst(JWT_TOKEN_HEADER);
        log.debug("Token: {}", token);
        assertThat(token).isNotBlank();
        assertThat(jwtVerifier.verify(token).getSubject()).isEqualTo(username);
    }

    @Test
    @Order(51)
    void loginUser_absentUser() {

        //given
        String password = "absentUserPass";
        String username = FAKER.name().username();
        String expectedMessage = "USERNAME / PASSWORD INCORRECT. PLEASE TRY AGAIN";

        //when
        User userLogin = User.builder()
                .username(username)
                .password(password)
                .build();
        var responseEntity = restTemplate.postForEntity("/user/login", userLogin, HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(400),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(BAD_REQUEST),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("BAD REQUEST"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage)
                ));
        String token = responseEntity.getHeaders().getFirst(JWT_TOKEN_HEADER);
        log.debug("Token: {}", token);
        assertThat(token).isNull();
    }

    @Test
    @Order(52)
    void loginUser_wrongPassword() {

        //given
        String password = "wrongPass";
        String username = user.getUsername();
        String expectedMessage = "USERNAME / PASSWORD INCORRECT. PLEASE TRY AGAIN";

        //when
        User userLogin = User.builder()
                .username(username)
                .password(password)
                .build();
        var responseEntity = restTemplate.postForEntity("/user/login", userLogin, HttpResponse.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(400),
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(BAD_REQUEST),
                        () -> assertThat(httpResponse.getReason()).isEqualTo("BAD REQUEST"),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage)
                ));
        String token = responseEntity.getHeaders().getFirst(JWT_TOKEN_HEADER);
        log.debug("Token: {}", token);
        assertThat(token).isNull();
    }

    @Test
    @Order(60)
    void loginUser_bruteForceDetectionTest() throws InterruptedException {

        //given
        User fakeUser = createRandomUser();
        String correctPassword = fakeUser.getPassword().replace("{noop}", "");
        String username = fakeUser.getUsername();
        userRepository.save(fakeUser);
        String wrongPassword = "wrongPass";

        //when
        User userLogin = User.builder()
                .username(username)
                .password(wrongPassword)
                .build();

        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {

            var responseEntity = restTemplate.postForEntity("/user/login", userLogin, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatusCode", 400)
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("reason", "BAD REQUEST")
                    .hasFieldOrPropertyWithValue("message", "USERNAME / PASSWORD INCORRECT. PLEASE TRY AGAIN");
        }

        for (int i = 0; i < 5; i++) {

            if (i > 3) {
                // Even correct password should not allow access to locked account
                userLogin = User.builder()
                        .username(username)
                        .password(correctPassword)
                        .build();
            }

            var responseEntity = restTemplate.postForEntity("/user/login", userLogin, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatusCode", 401)
                    .hasFieldOrPropertyWithValue("httpStatus", UNAUTHORIZED)
                    .hasFieldOrPropertyWithValue("reason", "UNAUTHORIZED")
                    .hasFieldOrPropertyWithValue("message", "YOUR ACCOUNT HAS BEEN LOCKED. PLEASE CONTACT ADMINISTRATION");
        }


    }
}