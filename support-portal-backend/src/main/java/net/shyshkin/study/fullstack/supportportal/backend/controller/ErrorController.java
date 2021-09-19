package net.shyshkin.study.fullstack.supportportal.backend.controller;

import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.shyshkin.study.fullstack.supportportal.backend.utility.HttpResponseUtility.createHttpResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class ErrorController {

    @GetMapping("/error")
    public ResponseEntity<HttpResponse> error() {
        return createHttpResponse(NOT_FOUND, "Resource not found");
    }
}
