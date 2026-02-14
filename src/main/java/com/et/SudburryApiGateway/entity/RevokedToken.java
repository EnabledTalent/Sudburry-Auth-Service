package com.et.SudburryApiGateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(
        name = "revoked_token",
        indexes = {
                @Index(name = "idx_revoked_token_hash", columnList = "tokenHash", unique = true),
                @Index(name = "idx_revoked_token_expires", columnList = "expiresAt")
        }
)
public class RevokedToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64, unique = true)
  private String tokenHash;

  @Column(nullable = false)
  private Date revokedAt;

  @Column(nullable = false)
  private Date expiresAt;

  public RevokedToken() {
  }

  public RevokedToken(String tokenHash, Date revokedAt, Date expiresAt) {
    this.tokenHash = tokenHash;
    this.revokedAt = revokedAt;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public Date getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Date revokedAt) {
    this.revokedAt = revokedAt;
  }

  public Date getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }
}

