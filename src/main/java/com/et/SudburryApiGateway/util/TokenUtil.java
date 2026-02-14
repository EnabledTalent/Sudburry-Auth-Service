package com.et.SudburryApiGateway.util;

import com.et.SudburryApiGateway.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


import java.security.Key;
import java.util.Date;

@Component
public class TokenUtil {
  private static String secret;

  // ✅ Proper injection into static field
  @Value("${jwt.secret}")
  public void setSecret(String secretValue) {
    TokenUtil.secret = secretValue;
  }

  private static Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public static String generateJwtToken(User user) {
    return Jwts.builder()
            .setSubject(user.getUsername())          // ✅ CORRECT
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000))
            .signWith(getSigningKey())                // ✅ CORRECT
            .compact();
  }

  public static Claims validateSignedToken(String token) {
    return Jwts.parserBuilder()                       // ✅ CORRECT
            .setSigningKey(getSigningKey())
            .setAllowedClockSkewSeconds(60)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
}


