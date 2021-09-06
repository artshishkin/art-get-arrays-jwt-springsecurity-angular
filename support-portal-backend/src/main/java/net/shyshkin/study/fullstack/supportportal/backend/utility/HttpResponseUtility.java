package net.shyshkin.study.fullstack.supportportal.backend.utility;

import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponseUtility {

    public static ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = HttpResponse.builder()
                .httpStatus(httpStatus)
                .httpStatusCode(httpStatus.value())
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .message(message.toUpperCase())
                .build();
        return new ResponseEntity<>(httpResponse, httpStatus);
    }

}
