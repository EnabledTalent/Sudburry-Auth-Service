package com.et.SudburryApiGateway.entity;

public class UserDTO {

  private String name;

  private String role;

  private String username;

  private String password;

  public UserDTO() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public UserDTO(String name, String role, String username, String password) {
    this.name = name;
    this.role = role;
    this.username = username;
    this.password = password;
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
}
