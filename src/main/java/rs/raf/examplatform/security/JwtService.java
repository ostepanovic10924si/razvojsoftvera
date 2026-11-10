package rs.raf.examplatform.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.raf.examplatform.model.User;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service public class JwtService {
    private final SecretKey key; private final long expirationMinutes;
    public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-minutes}") long expirationMinutes){
        if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMinutes=expirationMinutes;
    }
    public String create(User user){Instant now=Instant.now();return Jwts.builder().subject(user.getUsername()).claim("role",user.getRole().name()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationMinutes*60))).signWith(key).compact();}
    public String username(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
}
