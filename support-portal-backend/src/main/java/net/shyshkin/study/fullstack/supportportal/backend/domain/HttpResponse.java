package net.shyshkin.study.fullstack.supportportal.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HttpResponse {
    private int httpStatusCode; // 200, 201, 400, 500
    private HttpStatus httpStatus;
    private String reason;
    private String message;
}
