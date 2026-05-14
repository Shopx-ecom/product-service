package com.shopx.product.config;

import com.shopx.product.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sameer Shaikh
 * @date 03-04-2026
 * @description
 */

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry}")
    private long expiryMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token){
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public Long extractActorId(String token){
        Claims claims = extractAllClaims(token);
        return claims.get("actorId",Long.class);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        return claims.get("roles", List.class);
    }

    public Date extractExpiry(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiry(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        String userId = claims.getSubject();
        return userId.equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException e) {
            throw new AuthException("Token expired - "+e.getMessage());
        } catch (JwtException e) {
            throw new AuthException("Invalid token - "+e.getMessage());
        }
    }

}
