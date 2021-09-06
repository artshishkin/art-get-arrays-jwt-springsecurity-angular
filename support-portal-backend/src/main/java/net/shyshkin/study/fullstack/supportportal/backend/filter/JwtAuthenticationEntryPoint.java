package net.shyshkin.study.fullstack.supportportal.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        var httpResponse = HttpResponse.builder()
                .httpStatus(FORBIDDEN)
                .httpStatusCode(FORBIDDEN.value())
                .message(SecurityConstants.FORBIDDEN_MESSAGE)
                .reason(FORBIDDEN.getReasonPhrase().toUpperCase())
                .build();
        response.setStatus(FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, httpResponse);
    }
}
