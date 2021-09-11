package net.shyshkin.study.fullstack.supportportal.backend.common;

import com.github.javafaker.Faker;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.DEFAULT_USER_IMAGE_PATH;
import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.USER_IMAGE_FILENAME;
import static net.shyshkin.study.fullstack.supportportal.backend.domain.Role.ROLE_ADMIN;

@SpringBootTest
@ActiveProfiles("local")
public abstract class BaseUserTest {

    public static final Faker FAKER = Faker.instance();

    @Autowired
    protected UserRepository userRepository;

    protected static User user;

    protected User createRandomUser() {
        String userId = UUID.randomUUID().toString();
        return User.builder()
                .email(FAKER.bothify("????##@example.com"))
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .username(FAKER.name().username())
                .password("{noop}bad_password")
                .userId(userId)
                .isActive(true)
                .isNotLocked(true)
                .joinDate(LocalDateTime.now())
                .profileImageUrl(generateProfileImageUrl(userId))
                .lastLoginDate(LocalDateTime.now())
                .lastLoginDateDisplay(LocalDateTime.now())
                .role("ROLE_ADMIN")
                .authorities(ROLE_ADMIN.getAuthorities())
                .build();
    }

    protected UserDto createRandomUserDto() {
        return UserDto.builder()
                .email(FAKER.bothify("????##@example.com"))
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .username(FAKER.name().username())
                .isActive(true)
                .isNonLocked(true)
                .role(ROLE_ADMIN)
                .build();
    }

    private String generateProfileImageUrl(String userId) {
        return UriComponentsBuilder
                .fromUriString("http://localhost:8080")
                .path(DEFAULT_USER_IMAGE_PATH)
                .pathSegment(userId)
                .pathSegment(USER_IMAGE_FILENAME)
                .toUriString();
    }
}