
package com.et.SudburryApiGateway.config;

import com.et.SudburryApiGateway.service.TokenRevocationService;
import com.et.SudburryApiGateway.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private TokenRevocationService tokenRevocationService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    // If there's no Authorization header, just continue.
    // Spring Security will enforce authentication where required, and permitAll endpoints
    // (like /register) will work without being blocked by this filter.
    if (authHeader == null || authHeader.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract token from "Bearer <token>" format
    if (!authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String jwtToken = authHeader.substring(7);

    // VALIDATE THE TOKEN
    final Claims claims;
    try {
      claims = TokenUtil.validateSignedToken(jwtToken);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Invalid or expired JWT token");
      return;
    }

    if (tokenRevocationService != null && tokenRevocationService.isTokenRevoked(jwtToken)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("JWT token has been revoked");
      return;
    }

    String username = claims.getSubject();
    String role = claims.get("role", String.class);
    String authority = role == null ? "ROLE_USER" : (role.startsWith("ROLE_") ? role : "ROLE_" + role);
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui.html")
            || path.startsWith("/webjars")
            || path.startsWith("/register")
            || path.startsWith("/signin")
            || path.startsWith("/verifyRegistrationToken")
            || path.startsWith("/error");
  }
}
