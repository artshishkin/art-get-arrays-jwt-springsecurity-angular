package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.public-urls=**"
})
@ActiveProfiles("local")
class ErrorControllerTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void errorTest() {

        //when
        var responseEntity = restTemplate.getForEntity("/absent/endpoint", HttpResponse.class);

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(NOT_FOUND),
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(NOT_FOUND.value()),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo("RESOURCE NOT FOUND"),
                        () -> assertThat(httpResponse.getReason()).isEqualTo(NOT_FOUND.getReasonPhrase().toUpperCase()),
                        () -> assertThat(httpResponse.getTimestamp()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS))
                ));
    }
}