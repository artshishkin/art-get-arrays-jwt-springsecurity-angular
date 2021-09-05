package net.shyshkin.study.fullstack.supportportal.backend.common;

import com.github.javafaker.Faker;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("local")
public abstract class BaseUserTest {

    public static final Faker FAKER = Faker.instance();

    @Autowired
    protected UserRepository userRepository;

    protected User user;

    protected User createRandomUser() {
        return User.builder()
                .email(FAKER.bothify("????##@example.com"))
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .username(FAKER.name().username())
                .password("bad_password")
                .userId(UUID.randomUUID().toString())
                .isActive(true)
                .isNotLocked(true)
                .joinDate(LocalDateTime.now())
                .profileImageUrl("http://url_to_profile_img")
                .lastLoginDate(LocalDateTime.now())
                .lastLoginDateDisplay(LocalDateTime.now())
                .roles(new String[]{"ROLE_ADMIN", "ROLE_USER"})
                .authorities(new String[]{"user:delete", "user:read"})
                .build();
    }
}