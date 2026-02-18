package com.et.SudburryApiGateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "auth_user")
public class User {

  @Id
  @Column(nullable = false, length = 320)
  private String username;

  private String password;

  private boolean isEnabled;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private String role;

  public User(String username, String password, boolean isEnabled, String role, String name) {
    this.username = username;
    this.password = password;
    this.isEnabled = isEnabled;
    this.role = role;
    this.name=name;
  }

  public User() {

  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
