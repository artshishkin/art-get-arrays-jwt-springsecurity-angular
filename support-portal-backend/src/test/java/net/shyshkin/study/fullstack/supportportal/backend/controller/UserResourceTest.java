package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.utility.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceTest extends BaseUserTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
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
}