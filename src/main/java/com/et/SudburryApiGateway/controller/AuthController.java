package com.et.SudburryApiGateway.controller;

import com.et.SudburryApiGateway.service.TokenRevocationService;
import com.et.SudburryApiGateway.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class AuthController {

  @Autowired
  private TokenRevocationService tokenRevocationService;

  /**
   * JWT logout is stateless by default; this endpoint implements logout by
   * revoking (blacklisting) the presented JWT until it expires.
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Authorization header");
    }

    String jwt = authorizationHeader;
    if (authorizationHeader.startsWith("Bearer ")) {
      jwt = authorizationHeader.substring(7);
    }

    final Claims claims;
    try {
      claims = TokenUtil.validateSignedToken(jwt);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired JWT token");
    }

    Date expiresAt = claims.getExpiration();
    tokenRevocationService.revokeToken(jwt, expiresAt);

    return ResponseEntity.ok("Logged out");
  }
}

