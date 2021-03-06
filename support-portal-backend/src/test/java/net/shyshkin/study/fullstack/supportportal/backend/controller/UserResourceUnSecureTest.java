package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.public-urls=/**"
})
class UserResourceUnSecureTest extends BaseUserTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    public static final String USER_IMAGE_ENDPOINT_TEMPLATE = "/user/{userId}/profile-image";
    public static final String USER_DEFAULT_IMAGE_URI_TEMPLATE = USER_IMAGE_ENDPOINT_TEMPLATE;
    public static final String USER_CUSTOM_IMAGE_URI_TEMPLATE = "/user/{userId}/profile-image/{filename}";

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
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
                            null,
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
        void addNewUser_missedFirstName() {

            //given
            UserDto userDto = createRandomUserDto();
            Map<String, ?> paramMap = Map.of(
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
            );

            //when
            var responseEntity = restTemplate
                    .postForEntity(
                            "/user/add?username={username}&email={email}" +
                                    "&lastName={lastName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
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
                    .hasFieldOrPropertyWithValue("message", "Error(s) in parameters: [firstName:Should not be empty]");
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
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
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
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "Error(s) in parameters: [role:Failed to convert property value of type 'java.lang.String' to required type 'net.shyshkin.study.fullstack.supportportal.backend.domain.Role' for property 'role'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@javax.validation.constraints.NotNull net.shyshkin.study.fullstack.supportportal.backend.domain.Role] for value 'ROLE_FAKE'; nested exception is java.lang.IllegalArgumentException: No enum constant net.shyshkin.study.fullstack.supportportal.backend.domain.Role.ROLE_FAKE]");
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
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
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
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "Error(s) in parameters: [email:Must match email format]");
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
                    "isNotLocked", "not_a_boolean"
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
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "Error(s) in parameters: [notLocked:Failed to convert property value of type 'java.lang.String' to required type 'boolean' for property 'notLocked'; nested exception is java.lang.IllegalArgumentException: Invalid boolean value [not_a_boolean]]");
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
            UUID userId = user.getUserId();
            userDto.setUsername(currentUsername);
            userDto.setRole(Role.ROLE_MANAGER);

            Map<String, ?> paramMap = Map.of(
                    "userId", userId,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .exchange(
                            "/user/{userId}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
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
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
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
            UUID userId = user.getUserId();

            userDto.setRole(Role.ROLE_HR);

            Map<String, ?> paramMap = Map.of(
                    "userId", userId,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", userDto.isActive(),
                    "isNotLocked", userDto.isNotLocked()
            );

            //when
            ResponseEntity<User> responseEntity = restTemplate
                    .exchange(
                            "/user/{userId}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
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
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
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
            UUID userId = user.getUserId();

            userDto.setRole(Role.ROLE_USER);

            Map<String, ?> paramMap = Map.of(
                    "userId", userId,
                    "firstName", userDto.getFirstName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", userDto.isActive(),
                    "isNotLocked", userDto.isNotLocked()
            );

            //when
            var responseEntity = restTemplate
                    .exchange(
                            "/user/{userId}?username={username}&email={email}" +
                                    "&firstName={firstName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
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
                    .hasFieldOrPropertyWithValue("message", "Error(s) in parameters: [lastName:Should not be empty]");
        }

        @Test
        void updateUser_absentUser() {

            //given
            UserDto userDto = createRandomUserDto();
            String currentUsername = FAKER.name().username();
            UUID userId = UUID.randomUUID();
            userDto.setUsername(currentUsername);
            userDto.setRole(Role.ROLE_MANAGER);

            Map<String, ?> paramMap = Map.of(
                    "userId", userId,
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "username", userDto.getUsername(),
                    "email", userDto.getEmail(),
                    "role", userDto.getRole().name(),
                    "isActive", String.valueOf(userDto.isActive()),
                    "isNotLocked", String.valueOf(userDto.isNotLocked())
            );

            //when
            var responseEntity = restTemplate
                    .exchange(
                            "/user/{userId}?username={username}&email={email}" +
                                    "&firstName={firstName}&lastName={lastName}" +
                                    "&role={role}&active={isActive}&notLocked={isNotLocked}",
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
                    .hasFieldOrPropertyWithValue("message", "User not found");
        }

    }

    @Nested
    class FindUserTests {

        @BeforeEach
        void setUp() {
            user = userRepository
                    .findAll()
                    .stream()
                    .findAny()
                    .orElseGet(() -> userRepository.save(createRandomUser()));
        }

        @Test
        void findUser_present() {

            //given
            String username = user.getUsername();

            //when
            var responseEntity = restTemplate.getForEntity("/user/{username}", User.class, username);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                    .hasFieldOrPropertyWithValue("username", username)
                    .hasFieldOrPropertyWithValue("email", user.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", user.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", user.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", user.isActive())
                    .hasFieldOrPropertyWithValue("isNotLocked", user.isNotLocked())
                    .hasFieldOrPropertyWithValue("role", user.getRole());
        }

        @Test
        void findUser_absent() {

            //given
            String username = FAKER.name().username();

            //when
            var responseEntity = restTemplate.getForEntity("/user/{username}", HttpResponse.class, username);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", String.format("User with username `%s` not found", username));
        }
    }

    @Nested
    class GetAllUsersTests {

        @Test
        void getAllUsers() {

            //given
            long usersCount = userRepository.count();
            long DEFAULT_PAGEABLE_SIZE = 20;
            usersCount = Math.min(DEFAULT_PAGEABLE_SIZE, usersCount);

            //when
            var responseEntity = restTemplate.exchange("/user", HttpMethod.GET, null, UserPage.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull();
            assertThat(responseEntity.getBody().getContent())
                    .hasSize(Math.toIntExact(usersCount));
        }
    }

    @Data
    static class UserPage {
        private List<User> content;
        private boolean last;
        private boolean first;
        private int totalElements;
        private int size;
        private int numberOfElements;
        private int number;
        private boolean empty;
    }

    @Nested
    class ResetPasswordTests {

        @BeforeEach
        void setUp() {
            user = userRepository
                    .findAll()
                    .stream()
                    .findAny()
                    .orElseGet(() -> userRepository.save(createRandomUser()));
        }

        @Test
        void resetPassword() {

            //given
            String email = user.getEmail();

            //when
            var responseEntity = restTemplate.exchange("/user/resetPassword/{email}", HttpMethod.POST, null, HttpResponse.class, email);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", OK)
                    .hasFieldOrPropertyWithValue("message", "Password reset successfully. Check your email for new password");
        }
    }

    @Nested
    class UpdateProfileImageTests {

        @BeforeEach
        void setUp() {
            user = userRepository
                    .findAll()
                    .stream()
                    .findAny()
                    .orElseGet(() -> userRepository.save(createRandomUser()));
        }

        @Test
        void updateProfileImage_correct() throws IOException {

            //given
            UUID userId = user.getUserId();

            MultipartFile profileImage = new MockMultipartFile("profileImage", "test.png",
                    "image/png", ("Spring Framework" + UUID.randomUUID()).getBytes());

            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            body.add("profileImage", profileImage.getResource());

            //when
            var requestEntity = RequestEntity.put(USER_IMAGE_ENDPOINT_TEMPLATE, userId)
                    .contentType(MULTIPART_FORM_DATA)
                    .body(body);
            var responseEntity = restTemplate
                    .exchange(requestEntity, User.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                    .hasFieldOrPropertyWithValue("username", user.getUsername())
                    .hasFieldOrPropertyWithValue("email", user.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", user.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", user.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", user.isActive())
                    .hasFieldOrPropertyWithValue("isNotLocked", user.isNotLocked())
                    .hasFieldOrPropertyWithValue("role", user.getRole())
                    .satisfies(u -> assertThat(u.getProfileImageUrl()).endsWith(String.format(DEFAULT_USER_IMAGE_URI_PATTERN.concat("/avatar.jpg"), user.getUserId())));

            Path path = Path.of(USER_FOLDER, user.getUserId().toString(), USER_IMAGE_FILENAME);
            log.debug("Path of created file: {}", path);
            assertThat(Files.exists(path)).isTrue();
            assertThat(Files.getLastModifiedTime(path).toInstant()).isCloseTo(Instant.now(), within(100, ChronoUnit.MILLIS));
        }

        @Test
        void updateProfileImage_absentUser() {

            //given
            UUID userId = UUID.randomUUID();

            MultipartFile profileImage = new MockMultipartFile("profileImage", "test.txt",
                    "text/plain", ("Spring Framework" + UUID.randomUUID()).getBytes());

            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            body.add("profileImage", profileImage.getResource());

            //when
            var requestEntity = RequestEntity.put(USER_IMAGE_ENDPOINT_TEMPLATE, userId)
                    .contentType(MULTIPART_FORM_DATA)
                    .body(body);
            var responseEntity = restTemplate
                    .exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "User not found");
        }
    }

    @Nested
    class GetProfileImageTests {

        @BeforeEach
        void setUp() {
            user = userRepository
                    .findAll()
                    .stream()
                    .findAny()
                    .orElseGet(() -> userRepository.save(createRandomUser()));
        }

        @Test
        void getProfileImage_correct() throws IOException {

            //given
            UUID userId = user.getUserId();
            uploadProfileImage(userId);

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_CUSTOM_IMAGE_URI_TEMPLATE, userId, USER_IMAGE_FILENAME)
                    .accept(IMAGE_JPEG)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<byte[]>() {
            });

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody()).hasSize(52);
        }

        @Test
        void getProfileImage_absentUser() throws IOException {

            //given
            UUID userId = user.getUserId();
            uploadProfileImage(userId);
            UUID absentUserId = UUID.randomUUID();

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_DEFAULT_IMAGE_URI_TEMPLATE, absentUserId)
                    .accept(IMAGE_JPEG, APPLICATION_JSON)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "User not found");
        }

        @Test
        void getProfileImage_absentImage() {

            //given
            user = userRepository.save(createRandomUser());
            UUID userId = user.getUserId();

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_CUSTOM_IMAGE_URI_TEMPLATE, userId, USER_IMAGE_FILENAME)
                    .accept(IMAGE_JPEG, APPLICATION_JSON)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", INTERNAL_SERVER_ERROR)
                    .hasFieldOrPropertyWithValue("message", "Error occurred while processing file");
        }

        @Test
        void getImageById_correct() throws IOException {

            //given
            UUID userId = user.getUserId();
            uploadProfileImage(userId);
            String profileImageUrlFull = user.getProfileImageUrl();
            String profileImageUrl = profileImageUrlFull.substring(profileImageUrlFull.indexOf("/user/"));
            log.debug("Image URL: {}", profileImageUrl);

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(profileImageUrl)
                    .accept(IMAGE_JPEG)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<byte[]>() {
            });

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody()).hasSize(52);
        }


        @Test
        void getDefaultProfileImage_correct() throws IOException {

            //given
            UUID userId = user.getUserId();

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_DEFAULT_IMAGE_URI_TEMPLATE, userId)
                    .accept(IMAGE_JPEG)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<byte[]>() {
            });

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody()).hasSizeGreaterThan(52);
        }

        @Test
        void getDefaultProfileImage_absentUUID() {

            //given
            UUID userId = UUID.randomUUID();

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_DEFAULT_IMAGE_URI_TEMPLATE, userId)
                    .accept(IMAGE_JPEG, APPLICATION_JSON)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "User not found");
        }

        @Test
        void getDefaultProfileImage_not_a_UUID() {

            //given
            String userId = "not_a_UUID";

            //when
            RequestEntity<Void> requestEntity = RequestEntity.get(USER_DEFAULT_IMAGE_URI_TEMPLATE, userId)
                    .accept(IMAGE_JPEG, APPLICATION_JSON)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, HttpResponse.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("message", "Invalid UUID string: " + userId);
        }

        private void uploadProfileImage(UUID userId) throws IOException {

            MultipartFile profileImage = new MockMultipartFile("profileImage", "test.jpg",
                    IMAGE_JPEG_VALUE, ("Spring Framework" + UUID.randomUUID()).getBytes());

            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            body.add("profileImage", profileImage.getResource());

            //when
            var requestEntity = RequestEntity.put(USER_IMAGE_ENDPOINT_TEMPLATE, userId)
                    .contentType(MULTIPART_FORM_DATA)
                    .body(body);
            var responseEntity = restTemplate
                    .exchange(requestEntity, User.class);

            //then
            log.debug("Response Entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrPropertiesExcept("lastLoginDate", "lastLoginDateDisplay", "password", "id")
                    .hasFieldOrPropertyWithValue("userId", userId)
                    .hasFieldOrPropertyWithValue("username", user.getUsername())
                    .hasFieldOrPropertyWithValue("email", user.getEmail())
                    .hasFieldOrPropertyWithValue("firstName", user.getFirstName())
                    .hasFieldOrPropertyWithValue("lastName", user.getLastName())
                    .hasFieldOrPropertyWithValue("isActive", user.isActive())
                    .hasFieldOrPropertyWithValue("isNotLocked", user.isNotLocked())
                    .hasFieldOrPropertyWithValue("role", user.getRole())
                    .satisfies(u -> assertThat(u.getProfileImageUrl()).endsWith(String.format(DEFAULT_USER_IMAGE_URI_PATTERN.concat("/avatar.jpg"), user.getUserId())));

            Path path = Path.of(USER_FOLDER, user.getUserId().toString(), USER_IMAGE_FILENAME);
            log.debug("Path of created file: {}", path);
            assertThat(Files.exists(path)).isTrue();
            assertThat(Files.getLastModifiedTime(path).toInstant()).isCloseTo(Instant.now(), within(200, ChronoUnit.MILLIS));
        }
    }

}