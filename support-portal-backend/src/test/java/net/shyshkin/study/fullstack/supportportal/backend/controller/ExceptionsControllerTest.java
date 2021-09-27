package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.public-urls=**"
})
@ActiveProfiles("local")
class ExceptionsControllerTest {

    @Autowired
    TestRestTemplate restTemplate;

    @ParameterizedTest
    @CsvSource({
            "emailExists,This email is already taken",
            "userNotFound,The user was not found"
    })
    void badRequestException(String endpoint, String expectedMessage) {

        //when
        var responseEntity = restTemplate.getForEntity("/exceptions/{endpoint}", HttpResponse.class, endpoint);

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .satisfies(httpResponse -> assertAll(
                        () -> assertThat(httpResponse.getHttpStatus()).isEqualTo(BAD_REQUEST),
                        () -> assertThat(httpResponse.getHttpStatusCode()).isEqualTo(BAD_REQUEST.value()),
                        () -> assertThat(httpResponse.getMessage()).isEqualTo(expectedMessage),
                        () -> assertThat(httpResponse.getReason()).isEqualTo(BAD_REQUEST.getReasonPhrase().toUpperCase()),
                        () -> assertThat(httpResponse.getTimestamp()).isCloseTo(LocalDateTime.now(), within(200, ChronoUnit.MILLIS))
                ));
    }
}