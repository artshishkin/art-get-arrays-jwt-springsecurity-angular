package net.shyshkin.study.fullstack.supportportal.backend.common;

import com.github.javafaker.Faker;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.DEFAULT_USER_IMAGE_URI_PATTERN;
import static net.shyshkin.study.fullstack.supportportal.backend.domain.Role.ROLE_ADMIN;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MYSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public abstract class BaseUserTest {

    public static final Faker FAKER = Faker.instance();

    @Autowired
    protected UserRepository userRepository;

    protected static User user;

    protected User createRandomUser() {
        UUID userId = UUID.randomUUID();
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
                .isNotLocked(true)
                .role(ROLE_ADMIN)
                .build();
    }

    private String generateProfileImageUrl(UUID userId) {
        return UriComponentsBuilder
                .fromUriString("http://localhost:8080")
                .path(String.format(DEFAULT_USER_IMAGE_URI_PATTERN, userId))
//                .pathSegment(USER_IMAGE_FILENAME)
                .toUriString();
    }
}