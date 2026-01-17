package com.et.SudburryApiGateway.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.Date;


@Entity
public class VerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long Id;

  private String token;

  private Date expiryDate;

  @OneToOne
  private User user;

  public VerificationToken(long id, String token, Date expiryDate, User user) {
    Id = id;
    this.token = token;
    this.expiryDate = expiryDate;
    this.user = user;
  }

  public VerificationToken() {

  }

  public long getId() {
    return Id;
  }

  public void setId(long id) {
    Id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
