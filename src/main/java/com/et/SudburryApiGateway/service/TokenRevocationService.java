package com.et.SudburryApiGateway.service;

import com.et.SudburryApiGateway.entity.RevokedToken;
import com.et.SudburryApiGateway.repository.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class TokenRevocationService {

  @Autowired
  private RevokedTokenRepository revokedTokenRepository;

  public void revokeToken(String rawJwt, Date expiresAt) {
    if (rawJwt == null || rawJwt.isBlank()) return;
    if (expiresAt == null) return;

    String hash = sha256Hex(rawJwt);
    if (revokedTokenRepository.existsByTokenHash(hash)) {
      return; // already revoked
    }

    revokedTokenRepository.save(new RevokedToken(hash, new Date(), expiresAt));
  }

  public boolean isTokenRevoked(String rawJwt) {
    if (rawJwt == null || rawJwt.isBlank()) return false;
    String hash = sha256Hex(rawJwt);
    return revokedTokenRepository.existsByTokenHash(hash);
  }

  public long cleanupExpired() {
    return revokedTokenRepository.deleteByExpiresAtBefore(new Date());
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return toHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 is guaranteed in the JDK; wrap if something is very wrong.
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16));
      sb.append(Character.forDigit((b & 0xF), 16));
    }
    return sb.toString();
  }
}

