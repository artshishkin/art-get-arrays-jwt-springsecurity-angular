package net.shyshkin.study.fullstack.supportportal.backend.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants.*;

public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    public String generateJwtToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create()
                .withIssuer(GET_ARRAYS_LLC)
                .withAudience(GET_ARRAYS_ADMINISTRATION)
                .withIssuedAt(new Date())
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret));
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return new String[0];
    }
}
