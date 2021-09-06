package net.shyshkin.study.fullstack.supportportal.backend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants.GET_ARRAYS_LLC;

@Configuration
public class JwtConfig {

    @Bean
    public JWTVerifier jwtVerifier(@Value("${app.jwt.secret}") String secret) {

        Algorithm algorithm = Algorithm.HMAC512(secret);
        return JWT.require(algorithm)
                .withIssuer(GET_ARRAYS_LLC)
                .build(); //Reusable verifier instance
    }


}
