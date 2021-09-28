package net.shyshkin.study.fullstack.supportportal.backend.controller;

import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.service.LoginAttemptService;
import net.shyshkin.study.fullstack.supportportal.backend.utility.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants.JWT_TOKEN_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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

    private static String correctToken;

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
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
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
        String expectedMessage = ("Username `" + username + "` is already taken. Please select another one");

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
        String expectedMessage = ("User with email `" + email + "` is already registered");

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

        //when
        var userLogin = UserLoginDto.builder()
                .username(username)
                .password(password)
                .build();
        var responseEntity = restTemplate.postForEntity("/user/login", userLogin, User.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                .hasFieldOrPropertyWithValue("username", fakeUser.getUsername())
                .hasFieldOrPropertyWithValue("email", fakeUser.getEmail())
                .hasFieldOrPropertyWithValue("firstName", fakeUser.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", fakeUser.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN")
                .satisfies(u -> assertThat(u.getProfileImageUrl()).endsWith(String.format("/user/image/profile/%s", u.getUserId())));

        String token = responseEntity.getHeaders().getFirst(JWT_TOKEN_HEADER);
        log.debug("Token: {}", token);
        assertThat(token).isNotBlank();
        assertThat(jwtVerifier.verify(token).getSubject()).isEqualTo(username);
        correctToken = token;
    }

    @Test
    @Order(51)
    void loginUser_absentUser() {

        //given
        String password = "absentUserPass";
        String username = FAKER.name().username();
        String expectedMessage = "Username / password incorrect. Please try again";

        //when
        var userLogin = UserLoginDto.builder()
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
        String expectedMessage = "Username / password incorrect. Please try again";

        //when
        var userLogin = UserLoginDto.builder()
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
    void loginUser_bruteForceDetectionTest() {

        //given
        User fakeUser = createRandomUser();
        String correctPassword = fakeUser.getPassword().replace("{noop}", "");
        String username = fakeUser.getUsername();
        userRepository.save(fakeUser);
        String wrongPassword = "wrongPass";

        //when
        var userLogin = UserLoginDto.builder()
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
                    .hasFieldOrPropertyWithValue("message", "Username / password incorrect. Please try again");
        }

        for (int i = 0; i < 5; i++) {

            if (i > 3) {
                // Even correct password should not allow access to locked account
                userLogin = UserLoginDto.builder()
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
                    .hasFieldOrPropertyWithValue("message", "Your account has been locked. Please contact administration");
        }


    }

    @Test
    @Order(70)
    void addNewUser_throughRequestParam_correct() {

        //given
        UserDto userDto = createRandomUserDto();
        Map<String, ?> paramMap = Map.of(
                "firstName", userDto.getFirstName(),
                "lastName", userDto.getLastName(),
                "username", userDto.getUsername(),
                "email", userDto.getEmail(),
                "role", userDto.getRole().name(),
                "isActive", String.valueOf(userDto.isActive()),
                "isNotLocked", String.valueOf(userDto.isNotLocked())
        );

        //when
        var requestEntity = RequestEntity
                .post("/user/add")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(correctToken))
                .build();
        ResponseEntity<User> responseEntity = restTemplate
                .postForEntity(
                        "/user/add?username={username}&email={email}" +
                                "&firstName={firstName}&lastName={lastName}" +
                                "&role={role}&active={isActive}&notLocked={isNotLocked}",
                        requestEntity,
                        User.class,
                        paramMap
                );

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN");
    }

    @Test
    @Order(71)
    void addNewUser_withoutToken() {

        //given
        UserDto userDto = createRandomUserDto();
        Map<String, ?> paramMap = Map.of(
                "firstName", userDto.getFirstName(),
                "lastName", userDto.getLastName(),
                "username", userDto.getUsername(),
                "email", userDto.getEmail(),
                "role", userDto.getRole().name(),
                "isActive", userDto.isActive(),
                "isNotLocked", userDto.isNotLocked()
        );

        //when
        var responseEntity = restTemplate
                .postForEntity(
                        "/user/add?username={username}&email={email}" +
                                "&firstName={firstName}&lastName={lastName}" +
                                "&role={role}&active={isActive}&notLocked={isNotLocked}",
                        null,
                        HttpResponse.class,
                        paramMap
                );

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("httpStatus", FORBIDDEN)
                .hasFieldOrPropertyWithValue("message", "You need to log in to access this page");
    }

    @Test
    @Order(72)
    @DisplayName("When trying to use token of non existing user (or already deleted/blocked user) but token is correct then should allow access")
    void addNewUser_tokenOfNonExistingUser() {

        //given
        User nonExistingUser = createRandomUser();
        log.debug("Non existing user: {}", nonExistingUser);
        String token = jwtTokenProvider.generateJwtToken(new UserPrincipal(nonExistingUser));

        UserDto userDto = createRandomUserDto();
        Map<String, ?> paramMap = Map.of(
                "firstName", userDto.getFirstName(),
                "lastName", userDto.getLastName(),
                "username", userDto.getUsername(),
                "email", userDto.getEmail(),
                "role", userDto.getRole().name(),
                "isActive", userDto.isActive(),
                "isNotLocked", userDto.isNotLocked()
        );

        //when
        var requestEntity = RequestEntity
                .post("/user/add")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .build();
        ResponseEntity<User> responseEntity = restTemplate
                .postForEntity(
                        "/user/add?username={username}&email={email}" +
                                "&firstName={firstName}&lastName={lastName}" +
                                "&role={role}&active={isActive}&notLocked={isNotLocked}",
                        requestEntity,
                        User.class,
                        paramMap
                );

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN");
    }

    @Test
    @Order(72)
    @DisplayName("When trying to use totally invalid - 403 Forbidden")
    void addNewUser_invalidToken() {

        //given
        String token = "fake-token";

        UserDto userDto = createRandomUserDto();
        Map<String, ?> paramMap = Map.of(
                "firstName", userDto.getFirstName(),
                "lastName", userDto.getLastName(),
                "username", userDto.getUsername(),
                "email", userDto.getEmail(),
                "role", userDto.getRole().name(),
                "isActive", userDto.isActive(),
                "isNotLocked", userDto.isNotLocked()
        );

        //when
        var requestEntity = RequestEntity
                .post("/user/add")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .build();
        var responseEntity = restTemplate
                .postForEntity(
                        "/user/add?username={username}&email={email}" +
                                "&firstName={firstName}&lastName={lastName}" +
                                "&role={role}&active={isActive}&notLocked={isNotLocked}",
                        requestEntity,
                        HttpResponse.class,
                        paramMap
                );

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("httpStatus", FORBIDDEN)
                .hasFieldOrPropertyWithValue("message", "You need to log in to access this page");
    }

    @Test
    @Order(80)
    void addNewUser_throughFormData_correct() throws IOException {

        //given
        UserDto userDto = createRandomUserDto();

        MultipartFile profileImage = new MockMultipartFile("profileImage", "test.gif",
                "image/gif", ("Spring Framework" + UUID.randomUUID()).getBytes());

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();

        body.add("firstName", userDto.getFirstName());
        body.add("lastName", userDto.getLastName());
        body.add("username", userDto.getUsername());
        body.add("email", userDto.getEmail());
        body.add("role", userDto.getRole().name());
        body.add("active", userDto.isActive());
        body.add("notLocked", userDto.isNotLocked());
        body.add("profileImage", profileImage.getResource());

        //when
        var requestEntity = RequestEntity
                .post("/user/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(correctToken))
                .body(body);

        ResponseEntity<User> responseEntity = restTemplate
                .exchange(requestEntity, User.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN")
                .satisfies(u -> assertThat(u.getProfileImageUrl()).endsWith(String.format("/user/image/profile/%s/avatar.jpg", u.getUserId())));

        User createdUser = responseEntity.getBody();
        Path path = Path.of(FileConstant.USER_FOLDER, createdUser.getUserId().toString(), FileConstant.USER_IMAGE_FILENAME);
        log.debug("Path of created file: {}", path);
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.getLastModifiedTime(path).toInstant()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @Order(90)
    void updateUser_throughFormData_correct() throws IOException {

        //given
        User user = createRandomUser();
        userRepository.save(user);
        String currentUsername = user.getUsername();
        UUID userId = user.getUserId();

        UserDto userDto = createRandomUserDto();

        MultipartFile profileImage = new MockMultipartFile("profileImage", "test.jpeg",
                "image/jpeg", ("Spring Framework" + UUID.randomUUID()).getBytes());

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();

        body.add("firstName", userDto.getFirstName());
        body.add("lastName", userDto.getLastName());
        body.add("username", userDto.getUsername());
        body.add("email", userDto.getEmail());
        body.add("role", userDto.getRole().name());
        body.add("active", userDto.isActive());
        body.add("notLocked", userDto.isNotLocked());
        body.add("profileImage", profileImage.getResource());

        //when
        var requestEntity = RequestEntity
                .put("/user/{userId}", userId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(correctToken))
                .body(body);

        ResponseEntity<User> responseEntity = restTemplate
                .exchange(requestEntity, User.class);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                .hasFieldOrPropertyWithValue("username", userDto.getUsername())
                .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                .hasFieldOrPropertyWithValue("firstName", userDto.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", userDto.getLastName())
                .hasFieldOrPropertyWithValue("isActive", true)
                .hasFieldOrPropertyWithValue("isNotLocked", true)
                .hasFieldOrPropertyWithValue("role", "ROLE_ADMIN")
                .satisfies(u -> assertThat(u.getProfileImageUrl()).endsWith(String.format("/user/image/profile/%s/avatar.jpg", u.getUserId())));

        User createdUser = responseEntity.getBody();
        Path path = Path.of(FileConstant.USER_FOLDER, createdUser.getUserId().toString(), FileConstant.USER_IMAGE_FILENAME);
        log.debug("Path of created file: {}", path);
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.getLastModifiedTime(path).toInstant()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Nested
    class DeleteUserTests {

        @BeforeEach
        void setUp() {
            user = userRepository.save(createRandomUser());
        }

        @Test
        void deleteUser_ok_hasAuthority() {

            //given
            User superAdmin = createRandomUser();
            superAdmin.setRole(Role.ROLE_SUPER_ADMIN.name());
            superAdmin.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
            String token = jwtTokenProvider.generateJwtToken(new UserPrincipal(superAdmin));

            UUID userId = user.getUserId();

            //when
            var requestEntity = RequestEntity.delete("/user/{userId}", userId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", OK)
                    .hasFieldOrPropertyWithValue("message", "User deleted successfully");
        }

        @Test
        void deleteUser_unauthorized_has_NO_Authority() {

            //given
            User roleUser = createRandomUser();
            String token = jwtTokenProvider.generateJwtToken(new UserPrincipal(roleUser));

            UUID userId = user.getUserId();

            //when
            var requestEntity = RequestEntity.delete("/user/{userId}", userId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", FORBIDDEN)
                    .hasFieldOrPropertyWithValue("message", "You do not have enough permission");
        }

        @Test
        void deleteUser_badRequest_absentId() {

            //given
            User superAdmin = createRandomUser();
            superAdmin.setRole(Role.ROLE_SUPER_ADMIN.name());
            superAdmin.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
            String token = jwtTokenProvider.generateJwtToken(new UserPrincipal(superAdmin));

            String userId = UUID.randomUUID().toString();

            //when
            var requestEntity = RequestEntity.delete("/user/{userId}", userId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "User was not found");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class UserLoginDto {
        private String username;
        private String password;
    }
}