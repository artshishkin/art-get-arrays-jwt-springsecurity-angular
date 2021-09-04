package net.shyshkin.study.fullstack.supportportal.backend.domain;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class UserPrincipalTest {

    public static final Faker FAKER = Faker.instance();

    @Autowired
    UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        User fakeUser = createRandomUser();
        user = userRepository.save(fakeUser);
    }

    @Test
    void displayUser() {
        //given
        Long id = user.getId();
        int expectedAuthoritiesLength = user.getAuthorities().length + user.getRoles().length;

        //when
        Optional<User> savedUserOptional = userRepository.findById(id);
        assertThat(savedUserOptional)
                .hasValueSatisfying(userSaved -> assertThat(userSaved)
                        .hasNoNullFieldsOrProperties()
                        .satisfies(u -> log.debug("Saved User: {}", u)
                        )
                );

        Optional<UserPrincipal> userPrincipalOptional = savedUserOptional.map(UserPrincipal::new);

        //then
        assertThat(userPrincipalOptional)
                .hasValueSatisfying(userPrincipal -> assertThat(userPrincipal.getAuthorities())
                        .isNotNull()
                        .isNotEmpty()
                        .hasSize(expectedAuthoritiesLength)
                        .satisfies(authorities -> log.debug("Authorities: {}", authorities)));
    }

    private User createRandomUser() {
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