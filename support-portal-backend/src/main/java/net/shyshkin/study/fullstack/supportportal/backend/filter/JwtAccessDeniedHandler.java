package net.shyshkin.study.fullstack.supportportal.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        var httpResponse = HttpResponse.builder()
                .httpStatus(UNAUTHORIZED)
                .httpStatusCode(UNAUTHORIZED.value())
                .message(SecurityConstants.ACCESS_DENIED_MESSAGE)
                .reason(UNAUTHORIZED.getReasonPhrase().toUpperCase())
                .build();
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, httpResponse);
    }
}
