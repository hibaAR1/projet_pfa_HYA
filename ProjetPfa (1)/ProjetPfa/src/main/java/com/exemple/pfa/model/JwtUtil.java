package com.exemple.pfa.model;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final String secretKey = "kYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdRgUkXp2s5u8x/A?D(G+KbPe";
    private final SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    private final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();

    private final long accessTokenValidity = 60 * 60 * 1000; // 1 hour

    // Method to generate JWT token
    public String createToken(User user, List<String> roles) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Create claims
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        claims.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        claims.put("roles", roles != null ? roles : List.of());  // Ensure roles are not null, if not provided, set an empty list.

        // Set token creation and expiration times
        Date tokenCreateTime = new Date();
        Date tokenExpirationTime = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(accessTokenValidity));

        // Create and return JWT token
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(tokenExpirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Resolve token from the Authorization header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Parse the claims from the token
    public Claims getClaimsFromToken(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (JwtException ex) {
            throw new RuntimeException("Invalid JWT token", ex);
        }
    }

    // Validate if the token is expired or not
    public boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().after(new Date());
    }

    // Extract the roles from the claims
    public List<String> getRoles(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();  // Return an empty list if roles is null
    }

    // Extract email from the claims
    public String getEmail(Claims claims) {
        return claims.getSubject();
    }
}