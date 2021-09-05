package net.shyshkin.study.fullstack.supportportal.backend.domain;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UserPrincipalTest extends BaseUserTest {

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
}