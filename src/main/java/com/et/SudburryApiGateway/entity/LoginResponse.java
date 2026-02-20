package com.et.SudburryApiGateway.entity;

public class LoginResponse {

  private TokenEnvelope token;
  private boolean firstTimeLogin;

  public LoginResponse() {
  }

  public LoginResponse(TokenEnvelope token) {
    this.token = token;
  }

  public LoginResponse(TokenEnvelope token, boolean firstTimeLogin) {
    this.token = token;
    this.firstTimeLogin = firstTimeLogin;
  }

  public TokenEnvelope getToken() {
    return token;
  }

  public void setToken(TokenEnvelope token) {
    this.token = token;
  }

  public boolean isFirstTimeLogin() {
    return firstTimeLogin;
  }

  public void setFirstTimeLogin(boolean firstTimeLogin) {
    this.firstTimeLogin = firstTimeLogin;
  }

  public static class TokenEnvelope {
    private String token;
    private RoleEnvelope role;

    public TokenEnvelope() {
    }

    public TokenEnvelope(String token, RoleEnvelope role) {
      this.token = token;
      this.role = role;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    public RoleEnvelope getRole() {
      return role;
    }

    public void setRole(RoleEnvelope role) {
      this.role = role;
    }
  }

  public static class RoleEnvelope {
    private String role;

    public RoleEnvelope() {
    }

    public RoleEnvelope(String role) {
      this.role = role;
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }
  }
}

