package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Spring‑managed bean that produces and validates JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final Key key;

    /**
     * Constructor injection of the JWT secret and expiration properties.
     *
     * @param jwtSecret        the base‑64 or plain secret used for signing
     * @param jwtExpirationMs  the validity period (in millis) of a token
     */
    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expirationMs}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        // Create a strong HMAC‑SHA key from the secret string
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Creates a compact JWT string signed with the configured key.
     *
     * @param username the subject that will be stored in the token
     * @return the serialized JWT
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Parses the token and returns the embedded username.
     * <p>
     * If the token cannot be parsed (malformed, wrong signature, …) this method returns {@code null}.
     *
     * @param token the JWT string
     * @return the username (subject) or {@code null} on error
     */
    public String getUsernameFromJwt(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();          // same as .subject in Kotlin
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates that the supplied token is properly signed and not expired.
     *
     * @param token the JWT string
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

