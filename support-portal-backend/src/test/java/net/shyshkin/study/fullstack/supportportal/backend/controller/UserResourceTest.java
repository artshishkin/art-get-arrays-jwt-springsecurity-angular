package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class UserResourceTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void showUser_forbidden() {

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
}