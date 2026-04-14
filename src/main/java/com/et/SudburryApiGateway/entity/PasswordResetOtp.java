package com.et.SudburryApiGateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "password_reset_otp")
public class PasswordResetOtp {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 320, unique = true)
  private String username;

  /** BCrypt hash of the numeric OTP (never store plain OTP). */
  @Column(nullable = false, length = 80)
  private String otpHash;

  @Column(nullable = false)
  private Date expiryDate;

  public PasswordResetOtp() {
  }

  public PasswordResetOtp(String username, String otpHash, Date expiryDate) {
    this.username = username;
    this.otpHash = otpHash;
    this.expiryDate = expiryDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getOtpHash() {
    return otpHash;
  }

  public void setOtpHash(String otpHash) {
    this.otpHash = otpHash;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }
}
